// This file implements the JNI bindings for MemoryAllocator

#include <jni.h>

#include "include/org_rocksdb_MemoryAllocator.h"
#include "rocksdb/memory_allocator.h"
#include "rocksjni/cplusplus_to_java_convert.h"
#include "rocksjni/portal.h"

/*
 * Class:     org_rocksdb_MemoryAllocator_DefaultMemoryAllocator
 * Method:    newDefaultMemoryAllocatorInstance
 * Signature: ()J
 */
jlong Java_org_rocksdb_MemoryAllocator_00024DefaultMemoryAllocator_newDefaultMemoryAllocatorInstance(
    JNIEnv* /*env*/, jclass /*jcls*/) {
  // Create default memory allocator (nullptr means use system allocator)
  std::shared_ptr<ROCKSDB_NAMESPACE::MemoryAllocator>* allocator =
      new std::shared_ptr<ROCKSDB_NAMESPACE::MemoryAllocator>(nullptr);
  return GET_CPLUSPLUS_POINTER(allocator);
}

/*
 * Class:     org_rocksdb_MemoryAllocator_JEMallocMemoryAllocator
 * Method:    newJEMallocMemoryAllocatorInstance
 * Signature: ()J
 */
jlong Java_org_rocksdb_MemoryAllocator_00024JEMallocMemoryAllocator_newJEMallocMemoryAllocatorInstance(
    JNIEnv* env, jclass /*jcls*/) {
#ifdef ROCKSDB_JEMALLOC
  auto allocator = ROCKSDB_NAMESPACE::NewJemallocNodumpAllocator();
  if (allocator.ok()) {
    std::shared_ptr<ROCKSDB_NAMESPACE::MemoryAllocator>* allocator_ptr =
        new std::shared_ptr<ROCKSDB_NAMESPACE::MemoryAllocator>(
            std::move(allocator.value()));
    return GET_CPLUSPLUS_POINTER(allocator_ptr);
  } else {
    ROCKSDB_NAMESPACE::RocksDBExceptionJni::ThrowNew(
        env, "JEMalloc allocator creation failed: " + allocator.status().ToString());
    return 0;
  }
#else
  ROCKSDB_NAMESPACE::RocksDBExceptionJni::ThrowNew(
      env, "JEMalloc is not available in this build");
  return 0;
#endif
}

/*
 * Class:     org_rocksdb_MemoryAllocator
 * Method:    disposeInternalJni
 * Signature: (J)V
 */
void Java_org_rocksdb_MemoryAllocator_disposeInternalJni(JNIEnv* /*env*/,
                                                          jobject /*jobj*/,
                                                          jlong jhandle) {
  auto* allocator =
      reinterpret_cast<std::shared_ptr<ROCKSDB_NAMESPACE::MemoryAllocator>*>(
          jhandle);
  assert(allocator != nullptr);
  delete allocator;
}
