//
// Created by rhubner on 29-Nov-23.
//

#include "jni_merge_operator_v2.h"

#include "include/org_rocksdb_MergeOperatorV2.h"
#include "rocksjni/cplusplus_to_java_convert.h"
#include "rocksjni/portal.h"

jlong Java_org_rocksdb_MergeOperatorV2_toCString(JNIEnv* env, jclass,
                                                 jstring operator_name) {
  auto operator_name_utf = env->GetStringUTFChars(operator_name, nullptr);
  if (operator_name_utf == nullptr) {
    return 0;  // Exception
  }
  auto operator_name_len = env->GetStringUTFLength(operator_name);

  char* ret_value = new char[operator_name_len + 1];
  memcpy(ret_value, operator_name_utf, operator_name_len + 1);

  env->ReleaseStringUTFChars(operator_name, operator_name_utf);

  return GET_CPLUSPLUS_POINTER(ret_value);
}

jlong Java_org_rocksdb_MergeOperatorV2_newMergeOperator(
    JNIEnv* env, jobject java_merge_operator, jlong _operator_name) {
  char* operator_name = reinterpret_cast<char*>(_operator_name);

  auto* jni_merge_operator =
      new std::shared_ptr<ROCKSDB_NAMESPACE::MergeOperator>(
          new rocksdb::JniMergeOperatorV2(env, java_merge_operator,
                                          operator_name));

  return GET_CPLUSPLUS_POINTER(jni_merge_operator);
}

void Java_org_rocksdb_MergeOperatorV2_disposeInternal(JNIEnv*, jclass,
                                                      jlong j_handle) {
  auto* jni_merge_operator =
      reinterpret_cast<std::shared_ptr<ROCKSDB_NAMESPACE::MergeOperator>*>(
          j_handle);
  delete jni_merge_operator;
}

namespace ROCKSDB_NAMESPACE {

JniMergeOperatorV2::JniMergeOperatorV2(JNIEnv* env, jobject java_merge_operator,
                                       char* _operator_name)
    : JniCallback(env, java_merge_operator) {
  operator_name = _operator_name;

  j_merge_class = env->GetObjectClass(java_merge_operator);
  if (j_merge_class == nullptr) {
    return;  // Exception
  }
  j_merge_class = static_cast<jclass>(env->NewGlobalRef(j_merge_class));
  if (j_merge_class == nullptr) {
    if (env->ExceptionCheck() == JNI_FALSE) {
      RocksDBExceptionJni::ThrowNew(
          env, "Unable to obtain GlobalRef for merge operator");
    }
    return;
  }

  j_merge_internal =
      env->GetMethodID(j_merge_class, "mergeInternal",
                       "(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;[Ljava/nio/"
                       "ByteBuffer;)Lorg/rocksdb/MergeOperatorOutput;");
  if (j_merge_internal == nullptr) {
    return;
  }

  j_merge_internal_v3 =
      env->GetMethodID(j_merge_class, "mergeInternalV3",
                       "(Ljava/nio/ByteBuffer;[Lorg/rocksdb/WideColumn;[Ljava/nio/"
                       "ByteBuffer;)Lorg/rocksdb/MergeOperatorOutput;");
  if (j_merge_internal_v3 == nullptr) {
    return;
  }

  return_value_clazz = env->FindClass("org/rocksdb/MergeOperatorOutput");
  if (return_value_clazz == nullptr) {
    return;  // Exception
  }
  return_value_clazz =
      static_cast<jclass>(env->NewGlobalRef(return_value_clazz));
  if (return_value_clazz == nullptr) {
    return;  // Exception
  }

  return_value_method = env->GetMethodID(return_value_clazz, "getDirectValue",
                                         "()Ljava/nio/ByteBuffer;");
  if (return_value_method == nullptr) {
    return;
  }

  return_status_method =
      env->GetMethodID(return_value_clazz, "getOpStatus", "()I");
  if (return_status_method == nullptr) {
    return;
  }

  return_wide_columns_method =
      env->GetMethodID(return_value_clazz, "getWideColumns", "()[Lorg/rocksdb/WideColumn;");
  if (return_wide_columns_method == nullptr) {
    return;
  }

  is_wide_columns_method =
      env->GetMethodID(return_value_clazz, "isWideColumns", "()Z");
  if (is_wide_columns_method == nullptr) {
    return;
  }

  j_byte_buffer_class = ByteBufferJni::getJClass(env);
  if (j_byte_buffer_class == nullptr) {
    return;
  }
  j_byte_buffer_class =
      static_cast<jclass>(env->NewGlobalRef(j_byte_buffer_class));
  if (j_byte_buffer_class == nullptr) {
    return;  // Exception
  }

  byte_buffer_position =
      env->GetMethodID(j_byte_buffer_class, "position", "()I");
  if (byte_buffer_position == nullptr) {
    return;
  }

  byte_buffer_remaining =
      env->GetMethodID(j_byte_buffer_class, "remaining", "()I");
  if (byte_buffer_remaining == nullptr) {
    return;
  }

  return;
}

bool JniMergeOperatorV2::FullMergeV2(const MergeOperationInput& merge_in,
                                     MergeOperationOutput* merge_out) const {
  jboolean attached_thread = JNI_FALSE;
  auto env = getJniEnv(&attached_thread);

  auto j_operand_list =
      env->NewObjectArray(static_cast<jsize>(merge_in.operand_list.size()),
                          j_byte_buffer_class, nullptr);
  if (j_operand_list == nullptr) {
    return clean_and_return_error(attached_thread, merge_out);
  }

  for (auto i = 0u; i < merge_in.operand_list.size(); i++) {
    auto operand = merge_in.operand_list[i];
    auto byte_buffer = env->NewDirectByteBuffer(
        const_cast<void*>(reinterpret_cast<const void*>(operand.data())),
        operand.size());

    if (byte_buffer == nullptr) {
      return clean_and_return_error(attached_thread, merge_out);
    }
    env->SetObjectArrayElement(j_operand_list, i, byte_buffer);
  }

  auto key = env->NewDirectByteBuffer(
      const_cast<void*>(reinterpret_cast<const void*>(merge_in.key.data())),
      merge_in.key.size());
  if (key == nullptr) {
    return clean_and_return_error(attached_thread, merge_out);
  }

  jobject exising_value = nullptr;
  if (merge_in.existing_value != nullptr) {
    exising_value = env->NewDirectByteBuffer(
        const_cast<void*>(
            reinterpret_cast<const void*>(merge_in.existing_value->data())),
        merge_in.existing_value->size());
  }

  jobject result = env->CallObjectMethod(m_jcallback_obj, j_merge_internal, key,
                                         exising_value, j_operand_list);
  if (env->ExceptionCheck() == JNI_TRUE) {
    env->ExceptionClear();
    Error(merge_in.logger, "Unable to merge, Java code throw exception");
    return clean_and_return_error(attached_thread, merge_out);
  }

  if (result == nullptr) {
    Error(merge_in.logger, "Unable to merge, Java code return nullptr result");
    return clean_and_return_error(attached_thread, merge_out);
  }

  merge_out->op_failure_scope =
      javaToOpFailureScope(env->CallIntMethod(result, return_status_method));
  if (merge_out->op_failure_scope != MergeOperator::OpFailureScope::kDefault) {
    releaseJniEnv(attached_thread);
    return false;
  }

  auto result_byte_buff = env->CallObjectMethod(result, return_value_method);
  if (result_byte_buff == nullptr) {
    Error(merge_in.logger,
          "Unable to merge, Java code return nullptr ByteBuffer");
    return clean_and_return_error(attached_thread, merge_out);
  }

  auto result_byte_buff_data = env->GetDirectBufferAddress(result_byte_buff);

  auto position = env->CallIntMethod(result_byte_buff, byte_buffer_position);
  auto remaining = env->CallIntMethod(result_byte_buff, byte_buffer_remaining);

  merge_out->new_value.assign(
      static_cast<char*>(result_byte_buff_data) + position, remaining);

  releaseJniEnv(attached_thread);

  return true;
}

bool JniMergeOperatorV2::FullMergeV3(const MergeOperationInputV3& merge_in,
                                     MergeOperationOutputV3* merge_out) const {
  jboolean attached_thread = JNI_FALSE;
  auto env = getJniEnv(&attached_thread);

  // Convert operands
  auto j_operand_list =
      env->NewObjectArray(static_cast<jsize>(merge_in.operand_list.size()),
                          j_byte_buffer_class, nullptr);
  if (j_operand_list == nullptr) {
    releaseJniEnv(attached_thread);
    return false;
  }

  for (auto i = 0u; i < merge_in.operand_list.size(); i++) {
    auto operand = merge_in.operand_list[i];
    auto byte_buffer = env->NewDirectByteBuffer(
        const_cast<void*>(reinterpret_cast<const void*>(operand.data())),
        operand.size());
    if (byte_buffer == nullptr) {
      releaseJniEnv(attached_thread);
      return false;
    }
    env->SetObjectArrayElement(j_operand_list, i, byte_buffer);
  }

  // Convert key
  auto key = env->NewDirectByteBuffer(
      const_cast<void*>(reinterpret_cast<const void*>(merge_in.key.data())),
      merge_in.key.size());
  if (key == nullptr) {
    releaseJniEnv(attached_thread);
    return false;
  }

  // Convert existing value (variant type) to WideColumn array
  jobjectArray j_existing_columns = nullptr;
  std::visit(
      [&](auto&& arg) {
        using T = std::decay_t<decltype(arg)>;
        if constexpr (std::is_same_v<T, std::monostate>) {
          // No existing value
          j_existing_columns = nullptr;
        } else if constexpr (std::is_same_v<T, Slice>) {
          // Plain value - convert to single default column
          jclass wide_column_class = env->FindClass("org/rocksdb/WideColumn");
          if (wide_column_class != nullptr) {
            jmethodID constructor = env->GetMethodID(wide_column_class, "<init>", "([B[B)V");
            if (constructor != nullptr) {
              j_existing_columns = env->NewObjectArray(1, wide_column_class, nullptr);
              if (j_existing_columns != nullptr) {
                jbyteArray j_name = env->NewByteArray(0);  // Empty name = default column
                jbyteArray j_value = env->NewByteArray(static_cast<jsize>(arg.size()));
                if (j_name != nullptr && j_value != nullptr) {
                  env->SetByteArrayRegion(j_value, 0, static_cast<jsize>(arg.size()),
                                         reinterpret_cast<const jbyte*>(arg.data()));
                  jobject wide_column = env->NewObject(wide_column_class, constructor, j_name, j_value);
                  env->SetObjectArrayElement(j_existing_columns, 0, wide_column);
                  env->DeleteLocalRef(wide_column);
                }
                if (j_name != nullptr) env->DeleteLocalRef(j_name);
                if (j_value != nullptr) env->DeleteLocalRef(j_value);
              }
            }
            env->DeleteLocalRef(wide_column_class);
          }
        } else if constexpr (std::is_same_v<T, MergeOperationOutputV3::NewColumns>) {
          // Wide columns (vector of string pairs)
          jclass wide_column_class = env->FindClass("org/rocksdb/WideColumn");
          if (wide_column_class != nullptr) {
            jmethodID constructor = env->GetMethodID(wide_column_class, "<init>", "([B[B)V");
            if (constructor != nullptr) {
              j_existing_columns = env->NewObjectArray(static_cast<jsize>(arg.size()), wide_column_class, nullptr);
              if (j_existing_columns != nullptr) {
                for (size_t i = 0; i < arg.size(); i++) {
                  const auto& [name, value] = arg[i];
                  jbyteArray j_name = env->NewByteArray(static_cast<jsize>(name.size()));
                  jbyteArray j_value = env->NewByteArray(static_cast<jsize>(value.size()));
                  if (j_name != nullptr && j_value != nullptr) {
                    env->SetByteArrayRegion(j_name, 0, static_cast<jsize>(name.size()),
                                           reinterpret_cast<const jbyte*>(name.data()));
                    env->SetByteArrayRegion(j_value, 0, static_cast<jsize>(value.size()),
                                           reinterpret_cast<const jbyte*>(value.data()));
                    jobject wide_column = env->NewObject(wide_column_class, constructor, j_name, j_value);
                    env->SetObjectArrayElement(j_existing_columns, static_cast<jsize>(i), wide_column);
                    env->DeleteLocalRef(wide_column);
                  }
                  if (j_name != nullptr) env->DeleteLocalRef(j_name);
                  if (j_value != nullptr) env->DeleteLocalRef(j_value);
                }
              }
            }
            env->DeleteLocalRef(wide_column_class);
          }
        }
      },
      merge_in.existing_value);

  // Call Java mergeInternalV3
  jobject result = env->CallObjectMethod(m_jcallback_obj, j_merge_internal_v3, key,
                                         j_existing_columns, j_operand_list);
  if (env->ExceptionCheck() == JNI_TRUE) {
    env->ExceptionClear();
    releaseJniEnv(attached_thread);
    return false;
  }

  if (result == nullptr) {
    releaseJniEnv(attached_thread);
    return false;
  }

  // Check failure scope
  merge_out->op_failure_scope =
      javaToOpFailureScope(env->CallIntMethod(result, return_status_method));
  if (merge_out->op_failure_scope != MergeOperator::OpFailureScope::kDefault) {
    releaseJniEnv(attached_thread);
    return false;
  }

  // Check if result is wide columns or plain value
  jboolean is_wide = env->CallBooleanMethod(result, is_wide_columns_method);
  
  if (is_wide) {
    // Extract wide columns
    jobjectArray j_result_columns = static_cast<jobjectArray>(
        env->CallObjectMethod(result, return_wide_columns_method));
    if (j_result_columns != nullptr) {
      jsize num_columns = env->GetArrayLength(j_result_columns);
      MergeOperationOutputV3::NewColumns wide_columns;
      wide_columns.reserve(num_columns);
      
      jclass wide_column_class = env->FindClass("org/rocksdb/WideColumn");
      if (wide_column_class != nullptr) {
        jmethodID name_method = env->GetMethodID(wide_column_class, "name", "()[B");
        jmethodID value_method = env->GetMethodID(wide_column_class, "value", "()[B");
        
        if (name_method != nullptr && value_method != nullptr) {
          for (jsize i = 0; i < num_columns; i++) {
            jobject j_column = env->GetObjectArrayElement(j_result_columns, i);
            if (j_column != nullptr) {
              jbyteArray j_name = static_cast<jbyteArray>(env->CallObjectMethod(j_column, name_method));
              jbyteArray j_value = static_cast<jbyteArray>(env->CallObjectMethod(j_column, value_method));
              
              if (j_name != nullptr && j_value != nullptr) {
                jsize name_len = env->GetArrayLength(j_name);
                jsize value_len = env->GetArrayLength(j_value);
                
                jbyte* name_bytes = env->GetByteArrayElements(j_name, nullptr);
                jbyte* value_bytes = env->GetByteArrayElements(j_value, nullptr);
                
                if (name_bytes != nullptr && value_bytes != nullptr) {
                  // Create string pairs for NewColumns
                  wide_columns.emplace_back(
                      std::string(reinterpret_cast<char*>(name_bytes), name_len),
                      std::string(reinterpret_cast<char*>(value_bytes), value_len));
                }
                
                if (name_bytes != nullptr) env->ReleaseByteArrayElements(j_name, name_bytes, JNI_ABORT);
                if (value_bytes != nullptr) env->ReleaseByteArrayElements(j_value, value_bytes, JNI_ABORT);
              }
              
              if (j_name != nullptr) env->DeleteLocalRef(j_name);
              if (j_value != nullptr) env->DeleteLocalRef(j_value);
              env->DeleteLocalRef(j_column);
            }
          }
        }
        env->DeleteLocalRef(wide_column_class);
      }
      
      merge_out->new_value = std::move(wide_columns);
      env->DeleteLocalRef(j_result_columns);
    }
  } else {
    // Extract plain value
    auto result_byte_buff = env->CallObjectMethod(result, return_value_method);
    if (result_byte_buff != nullptr) {
      auto result_byte_buff_data = env->GetDirectBufferAddress(result_byte_buff);
      auto position = env->CallIntMethod(result_byte_buff, byte_buffer_position);
      auto remaining = env->CallIntMethod(result_byte_buff, byte_buffer_remaining);
      
      std::string new_value;
      new_value.assign(static_cast<char*>(result_byte_buff_data) + position, remaining);
      merge_out->new_value = std::move(new_value);
      
      env->DeleteLocalRef(result_byte_buff);
    }
  }

  releaseJniEnv(attached_thread);
  return true;
}

JniMergeOperatorV2::~JniMergeOperatorV2() {
  jboolean attached_thread = JNI_FALSE;
  auto env = getJniEnv(&attached_thread);
  env->DeleteGlobalRef(j_merge_class);
  env->DeleteGlobalRef(j_byte_buffer_class);
  env->DeleteGlobalRef(return_value_clazz);
  delete operator_name;
  releaseJniEnv(attached_thread);
}

bool JniMergeOperatorV2::clean_and_return_error(
    jboolean& attached_thread, MergeOperationOutput* merge_out) const {
  merge_out->op_failure_scope =
      MergeOperator::OpFailureScope::kOpFailureScopeMax;
  releaseJniEnv(attached_thread);
  return false;
}

MergeOperator::OpFailureScope JniMergeOperatorV2::javaToOpFailureScope(
    jint failure) const {
  switch (failure) {
    case 0:
      return MergeOperator::OpFailureScope::kDefault;
    case 1:
      return MergeOperator::OpFailureScope::kTryMerge;
    case 2:
      return MergeOperator::OpFailureScope::kMustMerge;
    case 3:
      return MergeOperator::OpFailureScope::kOpFailureScopeMax;
    default:
      return MergeOperator::OpFailureScope::kOpFailureScopeMax;
  }
}

const char* JniMergeOperatorV2::Name() const { return operator_name; }
}  // namespace ROCKSDB_NAMESPACE