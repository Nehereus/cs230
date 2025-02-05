package edu.uci.cs230.team10;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class IndexMerger {
    private final static Logger logger = Logger.getLogger(IndexMerger.class.getName());
    private static final String ROOT_DIRECTORY = "/home/hadoop/index";
    private final static Path mainIndexPath = Path.of(ROOT_DIRECTORY, "mainIndex");

    public static void main(String[] args) throws IOException {
        Directory mainIndex = FSDirectory.open(mainIndexPath);

        try (IndexWriter writer = new IndexWriter(mainIndex, new IndexWriterConfig());
             DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(ROOT_DIRECTORY), "index-*")) {
            for (Path subDir : stream) {

                final Path lockFile= Path.of(subDir.toString(), "write.lock");
                //remove write lock if it exists, assuming all updating has been done at this stage
                if (Files.exists(lockFile))
                    Files.delete(lockFile);
                Directory subIndex = FSDirectory.open(subDir);

                //open an index writer for the sub index to close the unclosed index;
                try(IndexWriter subIndexWriter = new IndexWriter(subIndex, new IndexWriterConfig())) {
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                writer.addIndexes(subIndex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
