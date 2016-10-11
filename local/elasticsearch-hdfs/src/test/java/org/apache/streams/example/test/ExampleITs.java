package org.apache.streams.example.test;

import org.apache.streams.elasticsearch.test.ElasticsearchPersistWriterIT;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ElasticsearchPersistWriterIT.class,
        ElasticsearchHdfsIT.class,
        HdfsElasticsearchIT.class,
})

public class ExampleITs {
    // the class remains empty,
    // used only as a holder for the above annotations
}