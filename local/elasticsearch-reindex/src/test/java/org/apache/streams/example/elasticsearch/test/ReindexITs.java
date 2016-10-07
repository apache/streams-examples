package org.apache.streams.example.elasticsearch.test;

import org.apache.streams.elasticsearch.test.ElasticsearchParentChildWriterIT;
import org.apache.streams.elasticsearch.test.ElasticsearchPersistWriterIT;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ElasticsearchPersistWriterIT.class,
        ElasticsearchParentChildWriterIT.class,
        ElasticsearchReindexIT.class,
        ElasticsearchReindexParentIT.class,
        ElasticsearchReindexChildIT.class
})

public class ReindexITs {
    // the class remains empty,
    // used only as a holder for the above annotations
}