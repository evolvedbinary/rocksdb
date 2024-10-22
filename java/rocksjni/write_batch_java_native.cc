// Copyright (c) 2011-present, Facebook, Inc.  All rights reserved.
//  This source code is licensed under both the GPLv2 (found in the
//  COPYING file in the root directory) and Apache 2.0 License
//  (found in the LICENSE.Apache file in the root directory).
//
// This file implements the "bridge" between Java and C++ and enables
// calling c++ ROCKSDB_NAMESPACE::WriteBatchJavaNative methods from Java side.

#include "rocksjni/write_batch_java_native.h"

#include "db/dbformat.h"
#include "include/org_rocksdb_WBWIRocksIterator.h"
#include "include/org_rocksdb_WriteBatchJavaNative.h"
#include "rocksdb/comparator.h"
#include "rocksjni/cplusplus_to_java_convert.h"
#include "rocksjni/portal.h"

/*
 * Class:     org_rocksdb_WriteBatchJavaNative
 * Method:    newWriteBatchJavaNative
 * Signature: (I)J
 */
jlong Java_org_rocksdb_WriteBatchJavaNative_newWriteBatchJavaNative(
    JNIEnv* /*env*/, jclass /*jcls*/, jint jreserved_bytes) {
  auto* wb = new ROCKSDB_NAMESPACE::WriteBatchJavaNative(
      static_cast<size_t>(jreserved_bytes));
  return GET_CPLUSPLUS_POINTER(wb);
}

/*
 * Class:     org_rocksdb_WriteBatchJavaNative
 * Method:    disposeInternalWriteBatchJavaNative
 * Signature: (J)V
 */
void Java_org_rocksdb_WriteBatchJavaNative_disposeInternalWriteBatchJavaNative(
    JNIEnv* /* env */, jclass /* jcls */, jlong handle) {
  auto* wb = reinterpret_cast<ROCKSDB_NAMESPACE::WriteBatchJavaNative*>(handle);
  assert(wb != nullptr);
  delete wb;
}

/**
 * @brief copy operations from Java-side write batch cache to C++ side write
 * batch
 *
 * @param wb write batch
 * @param bp
 */
void copy_write_batch_from_java(
    JNIEnv* env,
    ROCKSDB_NAMESPACE::WriteBatchJavaNative* wb,
    std::unique_ptr<ROCKSDB_NAMESPACE::WriteBatchJavaNativeBuffer>& bp) {
  while (bp->has_next()) {
    jint op = bp->next_int();
    switch (op) {
      case ROCKSDB_NAMESPACE::ValueType::kTypeValue: {
        jint key_len = bp->next_int();
        jint value_len = bp->next_int();

        // *** TODO (AP) how to handle exceptions here ?
        // *** pass in the message to bp->slice ?
        // *** throw Java exception like KVException

        ROCKSDB_NAMESPACE::Slice key_slice = bp->slice(key_len);
        ROCKSDB_NAMESPACE::Slice value_slice = bp->slice(value_len);

        ROCKSDB_NAMESPACE::Status status = wb->Put(key_slice, value_slice);
        if (!status.ok()) {
          ROCKSDB_NAMESPACE::RocksDBExceptionJni::ThrowNew(env, status);
          return;
        }
      } break;

      case ROCKSDB_NAMESPACE::ValueType::kTypeColumnFamilyValue: {
        jlong jcf_handle = bp->next_long();
        auto* cfh = reinterpret_cast<ROCKSDB_NAMESPACE::ColumnFamilyHandle*>(jcf_handle);

        jint key_len = bp->next_int();
        jint value_len = bp->next_int();
        auto key_slice = bp->slice(key_len);
        auto value_slice = bp->slice(value_len);

        auto status = wb->Put(cfh, key_slice, value_slice);
        if(!status.ok()) {
          ROCKSDB_NAMESPACE::RocksDBExceptionJni::ThrowNew(env, status);
          return;
        }
      } break;

      default: {
        ROCKSDB_NAMESPACE::RocksDBExceptionJni::ThrowNew(
            env, std::string("Unexpected writebatch command ")
                     .append(std::to_string(op)));
        return;
      } break;
    }
  }
}

/**
 * @brief 
 * 
 */
void Java_org_rocksdb_WriteBatchJavaNative_flushWriteBatchJavaNativeDirect(
    JNIEnv* env, jclass /*jcls*/, jlong jwb_handle, jlong jbuf_len,
    jobject jbuf) {
  auto* wb =
      reinterpret_cast<ROCKSDB_NAMESPACE::WriteBatchJavaNative*>(jwb_handle);
  assert(wb != nullptr);

  jbyte* buf = reinterpret_cast<jbyte*>(env->GetDirectBufferAddress(jbuf));

  if (env->ExceptionCheck()) {
    // exception thrown: OutOfMemoryError
    return;
  }
  auto bp = std::make_unique<ROCKSDB_NAMESPACE::WriteBatchJavaNativeBuffer>(
        buf, jbuf_len);

  copy_write_batch_from_java(env, wb, bp);
}

/*
 * Class:     org_rocksdb_WriteBatchJavaNative
 * Method:    flushWriteBatchJavaNativeArray
 * Signature: (JJ[B)V
 */
void Java_org_rocksdb_WriteBatchJavaNative_flushWriteBatchJavaNativeArray(
    JNIEnv* env, jclass /*jcls*/, jlong jwb_handle, jlong jbuf_len,
    jbyteArray jbuf) {
  auto* wb =
      reinterpret_cast<ROCKSDB_NAMESPACE::WriteBatchJavaNative*>(jwb_handle);
  assert(wb != nullptr);

  jbyte* buf = env->GetByteArrayElements(jbuf, nullptr);
  if (env->ExceptionCheck()) {
    // exception thrown: OutOfMemoryError
    return;
  }
  
    auto bp = std::make_unique<ROCKSDB_NAMESPACE::WriteBatchJavaNativeBuffer>(
        buf, jbuf_len);

    copy_write_batch_from_java(env, wb, bp);
  
    env->ReleaseByteArrayElements(jbuf, buf, JNI_ABORT);
}
