// Copyright (c) 2011-present, Facebook, Inc.  All rights reserved.
//  This source code is licensed under both the GPLv2 (found in the
//  COPYING file in the root directory) and Apache 2.0 License
//  (found in the LICENSE.Apache file in the root directory).
//
// This file implements the "bridge" between Java and C++ and enables
// calling c++ ROCKSDB_NAMESPACE::Iterator methods from Java side.

#include "rocksdb/iterator.h"

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>

#include <algorithm>

#include "include/org_rocksdb_RocksIterator.h"
#include "rocksjni/portal.h"

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    disposeInternal
 * Signature: (J)V
 */
void Java_org_rocksdb_RocksIterator_disposeInternalJni(JNIEnv* /*env*/,
                                                       jclass /*cls*/,
                                                       jlong handle) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  assert(it != nullptr);
  delete it;
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    isValid0
 * Signature: (J)Z
 */
jboolean Java_org_rocksdb_RocksIterator_isValid0Jni(JNIEnv* /*env*/,
                                                    jclass /*jcls*/,
                                                    jlong handle) {
  return reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle)->Valid();
}

jobject iterator_prefetch(JNIEnv* env, ROCKSDB_NAMESPACE::Iterator* it,
                          jobject jbuf) {
  const size_t kFlagsOffset = 0;
  const size_t kCountOffset = sizeof(jint);
  const size_t kEntriesOffset = 2 * sizeof(jint);

  size_t jbuf_len = static_cast<size_t>(env->GetDirectBufferCapacity(jbuf));
  void* jbuf_address = env->GetDirectBufferAddress(jbuf);
  if (jbuf_address == nullptr) {
    ROCKSDB_NAMESPACE::IllegalArgumentExceptionJni::ThrowNew(
        env, "Unable to access direct buffer address");
    return nullptr;
  }
  char* buf = reinterpret_cast<char*>(jbuf_address);

  size_t entryOffset = kEntriesOffset;
  jint count = 0;
  for (; it->Valid(); count++) {
    ROCKSDB_NAMESPACE::Slice key_slice = it->key();
    ROCKSDB_NAMESPACE::Slice value_slice = it->value();
    size_t key_size = key_slice.size();
    size_t value_size = value_slice.size();
    size_t key_size_align = key_size + 3 & ~0x3;
    size_t value_size_align = value_size + 3 & ~0x3;
    if (entryOffset + 0x8 + key_size_align + value_size_align >= jbuf_len) {
      // buffer is full. stop here.
      // there is a potential inefficiency if a few entries fill "most of" a
      // buffer
      // TODO (AP) fix the inefficiency - e.g. allocate/use an overflow buffer

      // we know that there is a Next()
      *reinterpret_cast<jint*>(buf + kFlagsOffset) = JNI_TRUE;
      *reinterpret_cast<jint*>(buf + kCountOffset) = count;
      return jbuf;
    }
    reinterpret_cast<jint*>(buf + entryOffset)[0] = static_cast<jint>(key_size);
    reinterpret_cast<jint*>(buf + entryOffset)[1] =
        static_cast<jint>(value_size);
    entryOffset += 0x8;
    std::memcpy(buf + entryOffset, key_slice.data(), key_size);
    entryOffset += key_size_align;
    std::memcpy(buf + entryOffset, value_slice.data(), value_size);
    entryOffset += value_size_align;

    it->Next();
  }
  // we know there is not a Next()
  *reinterpret_cast<jint*>(buf + kFlagsOffset) = JNI_FALSE;
  *reinterpret_cast<jint*>(buf + kCountOffset) = count;
  return jbuf;
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    seekToFirst0Jni
 * Signature: (J[BI)[B
 */
jobject Java_org_rocksdb_RocksIterator_seekToFirst0Jni(JNIEnv* env,
                                                       jclass /*jcls*/,
                                                       jlong handle,
                                                       jobject jbuf) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  it->SeekToFirst();
  return iterator_prefetch(env, it, jbuf);
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    seekToLast0
 * Signature: (J)V
 */
void Java_org_rocksdb_RocksIterator_seekToLast0Jni(JNIEnv* /*env*/,
                                                   jclass /*jcls*/,
                                                   jlong handle) {
  reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle)->SeekToLast();
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    next0Jni
 * Signature: (J[BI)[B
 */
jobject Java_org_rocksdb_RocksIterator_next0Jni(JNIEnv* env, jclass,
                                                jlong handle, jobject jbuf) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  // we don't call it->Next(); -we should already be at the next item,
  // after the previous instance of iterator_prefetch
  return iterator_prefetch(env, it, jbuf);
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    prev0
 * Signature: (J)V
 */
void Java_org_rocksdb_RocksIterator_prev0Jni(JNIEnv* /*env*/, jclass /*jobj*/,
                                             jlong handle) {
  reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle)->Prev();
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    refresh0
 * Signature: (J)V
 */
void Java_org_rocksdb_RocksIterator_refresh0Jni(JNIEnv* env, jclass /*jcls*/,
                                                jlong handle) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  ROCKSDB_NAMESPACE::Status s = it->Refresh();

  if (s.ok()) {
    return;
  }

  ROCKSDB_NAMESPACE::RocksDBExceptionJni::ThrowNew(env, s);
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    refresh1
 * Signature: (JJ)V
 */
void Java_org_rocksdb_RocksIterator_refresh1(JNIEnv* env, jobject /*jobj*/,
                                             jlong handle,
                                             jlong snapshot_handle) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  auto* snapshot =
      reinterpret_cast<ROCKSDB_NAMESPACE::Snapshot*>(snapshot_handle);
  ROCKSDB_NAMESPACE::Status s = it->Refresh(snapshot);

  if (s.ok()) {
    return;
  }

  ROCKSDB_NAMESPACE::RocksDBExceptionJni::ThrowNew(env, s);
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    seek0
 * Signature: (J[BI)V
 */
void Java_org_rocksdb_RocksIterator_seek0Jni(JNIEnv* env, jclass /*jcls*/,
                                             jlong handle, jbyteArray jtarget,
                                             jint jtarget_len) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  auto seek = [&it](ROCKSDB_NAMESPACE::Slice& target_slice) {
    it->Seek(target_slice);
  };
  ROCKSDB_NAMESPACE::JniUtil::k_op_region(seek, env, jtarget, 0, jtarget_len);
}

/*
 * This method supports fetching into indirect byte buffers;
 * the Java wrapper extracts the byte[] and passes it here.
 * In this case, the buffer offset of the key may be non-zero.
 *
 * Class:     org_rocksdb_RocksIterator
 * Method:    seek0
 * Signature: (J[BII)V
 */
void Java_org_rocksdb_RocksIterator_seekByteArray0Jni(
    JNIEnv* env, jclass /*jcls*/, jlong handle, jbyteArray jtarget,
    jint jtarget_off, jint jtarget_len) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  auto seek = [&it](ROCKSDB_NAMESPACE::Slice& target_slice) {
    it->Seek(target_slice);
  };
  ROCKSDB_NAMESPACE::JniUtil::k_op_region(seek, env, jtarget, jtarget_off,
                                          jtarget_len);
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    seekDirect0
 * Signature: (JLjava/nio/ByteBuffer;II)V
 */
void Java_org_rocksdb_RocksIterator_seekDirect0Jni(JNIEnv* env, jclass /*jobj*/,
                                                   jlong handle,
                                                   jobject jtarget,
                                                   jint jtarget_off,
                                                   jint jtarget_len) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  auto seek = [&it](ROCKSDB_NAMESPACE::Slice& target_slice) {
    it->Seek(target_slice);
  };
  ROCKSDB_NAMESPACE::JniUtil::k_op_direct(seek, env, jtarget, jtarget_off,
                                          jtarget_len);
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    seekForPrevDirect0
 * Signature: (JLjava/nio/ByteBuffer;II)V
 */
void Java_org_rocksdb_RocksIterator_seekForPrevDirect0Jni(
    JNIEnv* env, jclass /*jcls*/, jlong handle, jobject jtarget,
    jint jtarget_off, jint jtarget_len) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  auto seekPrev = [&it](ROCKSDB_NAMESPACE::Slice& target_slice) {
    it->SeekForPrev(target_slice);
  };
  ROCKSDB_NAMESPACE::JniUtil::k_op_direct(seekPrev, env, jtarget, jtarget_off,
                                          jtarget_len);
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    seekForPrev0
 * Signature: (J[BI)V
 */
void Java_org_rocksdb_RocksIterator_seekForPrev0Jni(JNIEnv* env,
                                                    jclass /*jcls*/,
                                                    jlong handle,
                                                    jbyteArray jtarget,
                                                    jint jtarget_len) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  auto seek = [&it](ROCKSDB_NAMESPACE::Slice& target_slice) {
    it->SeekForPrev(target_slice);
  };
  ROCKSDB_NAMESPACE::JniUtil::k_op_region(seek, env, jtarget, 0, jtarget_len);
}

/*
 * This method supports fetching into indirect byte buffers;
 * the Java wrapper extracts the byte[] and passes it here.
 * In this case, the buffer offset of the key may be non-zero.
 *
 * Class:     org_rocksdb_RocksIterator
 * Method:    seek0
 * Signature: (J[BII)V
 */
void Java_org_rocksdb_RocksIterator_seekForPrevByteArray0Jni(
    JNIEnv* env, jclass /*jcls*/, jlong handle, jbyteArray jtarget,
    jint jtarget_off, jint jtarget_len) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  auto seek = [&it](ROCKSDB_NAMESPACE::Slice& target_slice) {
    it->SeekForPrev(target_slice);
  };
  ROCKSDB_NAMESPACE::JniUtil::k_op_region(seek, env, jtarget, jtarget_off,
                                          jtarget_len);
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    status0
 * Signature: (J)V
 */
void Java_org_rocksdb_RocksIterator_status0Jni(JNIEnv* env, jclass /*jcls*/,
                                               jlong handle) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  ROCKSDB_NAMESPACE::Status s = it->status();

  if (s.ok()) {
    return;
  }

  ROCKSDB_NAMESPACE::RocksDBExceptionJni::ThrowNew(env, s);
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    key0
 * Signature: (J)[B
 */
jbyteArray Java_org_rocksdb_RocksIterator_key0(JNIEnv* env, jclass /*jcls*/,
                                               jlong handle) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  ROCKSDB_NAMESPACE::Slice key_slice = it->key();

  jbyteArray jkey = env->NewByteArray(static_cast<jsize>(key_slice.size()));
  if (jkey == nullptr) {
    // exception thrown: OutOfMemoryError
    return nullptr;
  }
  env->SetByteArrayRegion(
      jkey, 0, static_cast<jsize>(key_slice.size()),
      const_cast<jbyte*>(reinterpret_cast<const jbyte*>(key_slice.data())));
  return jkey;
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    keyDirect0
 * Signature: (JLjava/nio/ByteBuffer;II)I
 */
jint Java_org_rocksdb_RocksIterator_keyDirect0(JNIEnv* env, jclass /*jcls*/,
                                               jlong handle, jobject jtarget,
                                               jint jtarget_off,
                                               jint jtarget_len) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  ROCKSDB_NAMESPACE::Slice key_slice = it->key();
  return ROCKSDB_NAMESPACE::JniUtil::copyToDirect(env, key_slice, jtarget,
                                                  jtarget_off, jtarget_len);
}

/*
 * This method supports fetching into indirect byte buffers;
 * the Java wrapper extracts the byte[] and passes it here.
 *
 * Class:     org_rocksdb_RocksIterator
 * Method:    keyByteArray0
 * Signature: (J[BII)I
 */
jint Java_org_rocksdb_RocksIterator_keyByteArray0(JNIEnv* env, jclass /*jcls*/,
                                                  jlong handle, jbyteArray jkey,
                                                  jint jkey_off,
                                                  jint jkey_len) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  ROCKSDB_NAMESPACE::Slice key_slice = it->key();
  jsize copy_size = std::min(static_cast<uint32_t>(key_slice.size()),
                             static_cast<uint32_t>(jkey_len));
  env->SetByteArrayRegion(
      jkey, jkey_off, copy_size,
      const_cast<jbyte*>(reinterpret_cast<const jbyte*>(key_slice.data())));

  return static_cast<jsize>(key_slice.size());
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    value0
 * Signature: (J)[B
 */
jbyteArray Java_org_rocksdb_RocksIterator_value0(JNIEnv* env, jclass /*jcls*/,
                                                 jlong handle) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  ROCKSDB_NAMESPACE::Slice value_slice = it->value();

  jbyteArray jkeyValue =
      env->NewByteArray(static_cast<jsize>(value_slice.size()));
  if (jkeyValue == nullptr) {
    // exception thrown: OutOfMemoryError
    return nullptr;
  }
  env->SetByteArrayRegion(
      jkeyValue, 0, static_cast<jsize>(value_slice.size()),
      const_cast<jbyte*>(reinterpret_cast<const jbyte*>(value_slice.data())));
  return jkeyValue;
}

/*
 * Class:     org_rocksdb_RocksIterator
 * Method:    valueDirect0
 * Signature: (JLjava/nio/ByteBuffer;II)I
 */
jint Java_org_rocksdb_RocksIterator_valueDirect0(JNIEnv* env, jclass /*jcls*/,
                                                 jlong handle, jobject jtarget,
                                                 jint jtarget_off,
                                                 jint jtarget_len) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  ROCKSDB_NAMESPACE::Slice value_slice = it->value();
  return ROCKSDB_NAMESPACE::JniUtil::copyToDirect(env, value_slice, jtarget,
                                                  jtarget_off, jtarget_len);
}

/*
 * This method supports fetching into indirect byte buffers;
 * the Java wrapper extracts the byte[] and passes it here.
 *
 * Class:     org_rocksdb_RocksIterator
 * Method:    valueByteArray0
 * Signature: (J[BII)I
 */
jint Java_org_rocksdb_RocksIterator_valueByteArray0(
    JNIEnv* env, jclass /*jcls*/, jlong handle, jbyteArray jvalue_target,
    jint jvalue_off, jint jvalue_len) {
  auto* it = reinterpret_cast<ROCKSDB_NAMESPACE::Iterator*>(handle);
  ROCKSDB_NAMESPACE::Slice value_slice = it->value();
  jsize copy_size = std::min(static_cast<uint32_t>(value_slice.size()),
                             static_cast<uint32_t>(jvalue_len));
  env->SetByteArrayRegion(
      jvalue_target, jvalue_off, copy_size,
      const_cast<jbyte*>(reinterpret_cast<const jbyte*>(value_slice.data())));

  return static_cast<jsize>(value_slice.size());
}
