
#include <cstring>

#include "db/db_test_util.h"
#include "options/options_helper.h"
#include "port/stack_trace.h"
#include "rocksdb/filter_policy.h"
#include "rocksdb/flush_block_policy.h"
#include "rocksdb/merge_operator.h"
#include "rocksdb/perf_context.h"
#include "rocksdb/table.h"
#include "rocksdb/utilities/debug.h"
#include "table/block_based/block_based_table_reader.h"
#include "table/block_based/block_builder.h"
#include "test_util/sync_point.h"
#include "util/file_checksum_helper.h"
#include "util/random.h"
#include "utilities/counted_fs.h"
#include "utilities/fault_injection_env.h"
#include "utilities/fault_injection_fs.h"
#include "utilities/merge_operators.h"
#include "utilities/merge_operators/string_append/stringappend.h"


namespace ROCKSDB_NAMESPACE {


class BlobTest : public DBTestBase {
 public:
  BlobTest() : DBTestBase("blob_test", /*env_do_fsync=*/false) {}

};

TEST_F(BlobTest, ReadOnlyWithBlob) {
  const int min_blob_size = 1000;
  // const int blob_size = min_blob_size + 10;
  const int blob_size = min_blob_size;
  const auto db_path = "c:\\tmp";
  const auto checkpoint_path = "c:\\tmp\\checkpoint";

  Options options = CurrentOptions();
  options.create_if_missing = true;
  options.enable_blob_files = true;
  options.min_blob_size = min_blob_size;

  DB* db2 = nullptr;

  ASSERT_OK(DB::Open(options, db_path, &db2));
//  ASSERT_OK(db2->Put(WriteOptions(), Slice("key"), Slice("value")));
//
  std::string read_result;
//  Status readStatus = db2->Get(ReadOptions(), Slice("key"), &read_result);
//  EXPECT_EQ(std::string("value"), read_result);

  auto big_value = std::make_unique<char[]>(blob_size);
  for (int i = 0; i < blob_size; i++) {
    big_value[i] = 'a';
  }
  ASSERT_OK(db2->Put(WriteOptions(), Slice("key2"),
                     Slice(big_value.get(), blob_size)));
  ASSERT_OK(db2->Get(ReadOptions(), Slice("key2"), &read_result));
  ASSERT_EQ(std::string(big_value.get(), blob_size), read_result);

  Checkpoint* checkpoint;
  ASSERT_OK(Checkpoint::Create(db2, &checkpoint));
  ASSERT_OK(checkpoint->CreateCheckpoint(checkpoint_path));

  delete checkpoint;

  db2->Close();
  delete db2;

  DB* db3 = nullptr;

  ASSERT_OK(DB::OpenForReadOnly(options, checkpoint_path, &db3, false));

  //  ASSERT_OK(db2->Get(ReadOptions(), Slice("key2"), &read_result));
  //  ASSERT_EQ(std::string(big_value.get(), blob_size), read_result);
  //  ASSERT_OK(db2->Get(ReadOptions(), Slice("key2"), &read_result));

  PinnableSlice result_slice;
  ASSERT_OK(db3->Get(ReadOptions(), db3->DefaultColumnFamily(), Slice("key2"),
                     &result_slice));
  ASSERT_EQ(Slice(big_value.get(), blob_size).ToString(), result_slice.ToString());

  db3->Close();
  delete db3;
}
}

int main(int argc, char** argv) {
  ROCKSDB_NAMESPACE::port::InstallStackTraceHandler();
  ::testing::InitGoogleTest(&argc, argv);
  RegisterCustomObjects(argc, argv);
  return RUN_ALL_TESTS();
}
