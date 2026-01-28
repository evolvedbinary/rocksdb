// This file implements the JNI bindings for RibbonFilter

#include <jni.h>

#include "include/org_rocksdb_RibbonFilter.h"
#include "rocksdb/filter_policy.h"
#include "rocksjni/cplusplus_to_java_convert.h"

/*
 * Class:     org_rocksdb_RibbonFilter
 * Method:    createNewRibbonFilter
 * Signature: (DI)J
 */
jlong Java_org_rocksdb_RibbonFilter_createNewRibbonFilter(
    JNIEnv* /*env*/, jclass /*jcls*/, jdouble jbits_per_key,
    jint jbloom_before_level) {
  auto* filter_policy =
      new std::shared_ptr<const ROCKSDB_NAMESPACE::FilterPolicy>(
          ROCKSDB_NAMESPACE::NewRibbonFilterPolicy(
              static_cast<double>(jbits_per_key),
              static_cast<int>(jbloom_before_level)));
  return GET_CPLUSPLUS_POINTER(filter_policy);
}