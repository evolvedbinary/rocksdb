
#include <cstring>
#include "db/db_test_util.h"

namespace ROCKSDB_NAMESPACE {

class BlobTest : public DBTestBase {
 public:
  BlobTest() : DBTestBase("blob_test", /*env_do_fsync=*/false) {}

};

TEST_F(BlobTest, BlobSnapshotError) {

  const int blob_size = 1000;
  //auto options = CurrentOptions(); // Everything works when we create options with this method.
  auto options = ROCKSDB_NAMESPACE::Options();
  options.create_if_missing = true;
  options.enable_blob_files = true;
  options.min_blob_size = blob_size;

  std::string path = "c:\\tmp\\";
  std::string checkpointPath = path + "\\checkpoint";

  auto big_value = std::make_unique<char[]>(blob_size);
  for (int i = 0; i < blob_size; i++) {
    big_value[i] = 'a';
  }

  auto value = Slice(big_value.get(), blob_size);
  auto key = Slice("some_key");

  { // Create DB, Write data and create checkpoint.
    DB* db = nullptr;

    ASSERT_OK(rocksdb::DB::Open(options, path, &db));
    ASSERT_OK(db->Put(rocksdb::WriteOptions(),key, value ));

    PinnableSlice result_slice;
    ASSERT_OK(db->Get(rocksdb::ReadOptions(), db->DefaultColumnFamily(), key,
                      &result_slice));  //Verify data are in DB
    result_slice.Reset();

    Checkpoint* checkpoint;
    ASSERT_OK(Checkpoint::Create(db, &checkpoint));
    ASSERT_OK(checkpoint->CreateCheckpoint(checkpointPath));
    delete checkpoint;

    ASSERT_OK(db->Close());
    delete db;
  }

  { // Open checkpoint as read only
    DB* db = nullptr;
    ASSERT_OK(rocksdb::DB::OpenForReadOnly(options, checkpointPath, &db, true));
    PinnableSlice result_slice;
    ASSERT_OK(db->Get(rocksdb::ReadOptions(), db->DefaultColumnFamily(), key,
                       &result_slice));
    result_slice.Reset();
    db->Close();
    delete db;

  }
}
}

int main(int argc, char** argv) {
  ROCKSDB_NAMESPACE::port::InstallStackTraceHandler();
  ::testing::InitGoogleTest(&argc, argv);
  RegisterCustomObjects(argc, argv);
  return RUN_ALL_TESTS();
}
