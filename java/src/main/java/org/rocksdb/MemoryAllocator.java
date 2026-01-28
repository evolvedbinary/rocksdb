// Copyright (c) 2011-present, Facebook, Inc.  All rights reserved.
//  This source code is licensed under both the GPLv2 (found in the
//  COPYING file in the root directory) and Apache 2.0 License
//  (found in the LICENSE.Apache file in the root directory).

package org.rocksdb;

/**
 * MemoryAllocator is a custom memory allocator that can be used by RocksDB
 * for cache blocks and other memory allocations. This allows using custom
 * allocators like JEMalloc for better memory management and reduced fragmentation.
 * <p>
 * As a descendent of {@link RocksObject}, this class is {@link AutoCloseable}
 * and will be automatically released if opened in the preamble of a try with
 * resources block.
 */
public abstract class MemoryAllocator extends RocksObject {

  /**
   * Constructor for MemoryAllocator
   *
   * @param nativeHandle the native handle to the C++ MemoryAllocator object
   */
  protected MemoryAllocator(final long nativeHandle) {
    super(nativeHandle);
  }

  @Override
  protected final void disposeInternal(final long handle) {
    disposeInternalJni(handle);
  }

  /**
   * Creates a default system memory allocator
   *
   * @return a new DefaultMemoryAllocator instance
   */
  public static MemoryAllocator createDefault() {
    return new DefaultMemoryAllocator();
  }

  /**
   * Creates a JEMalloc-based memory allocator.
   * This allocator can help reduce memory fragmentation.
   * Note: Requires JEMalloc to be available on the system.
   *
   * @return a new JEMallocMemoryAllocator instance
   * @throws UnsupportedOperationException if JEMalloc is not available
   */
  public static MemoryAllocator createJEMalloc() {
    return new JEMallocMemoryAllocator();
  }

  /**
   * Default memory allocator using system malloc/free
   */
  private static class DefaultMemoryAllocator extends MemoryAllocator {
    private DefaultMemoryAllocator() {
      super(newDefaultMemoryAllocatorInstance());
    }

    private static native long newDefaultMemoryAllocatorInstance();
  }

  /**
   * JEMalloc-based memory allocator for reduced fragmentation
   */
  private static class JEMallocMemoryAllocator extends MemoryAllocator {
    private JEMallocMemoryAllocator() {
      super(newJEMallocMemoryAllocatorInstance());
    }

    private static native long newJEMallocMemoryAllocatorInstance();
  }

  private static native void disposeInternalJni(final long handle);
}
