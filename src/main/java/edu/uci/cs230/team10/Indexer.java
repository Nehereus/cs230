package edu.uci.cs230.team10;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Indexer extends Reducer<Text, Text, NullWritable, NullWritable> {

    private static final String ROOT_DIRECTORY = "/home/hadoop/index";
    private  IndexWriter writer;
    private  StandardAnalyzer analyzer;
    private  Directory index;

    protected void setWriter(IndexWriter writer) {
        this.writer = writer;
    }

    protected void setup(Context context) throws IOException {
        Path rootDir = Path.of(ROOT_DIRECTORY);
        Files.createDirectories(rootDir); // Ensure the root directory exists
        Path chosenDir = null;
        // Check existing subdirectories for availability
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir, "index-*")) {
            for (Path subDir : stream) {
                Path lockFile = subDir.resolve("write.lock");

                if (Files.isDirectory(subDir) && !Files.exists(lockFile)) {
                    chosenDir = subDir;
                    System.out.println("Reusing existing directory: " + subDir);
                    break;
                } else {
                    System.out.println("Directory " + subDir + " is locked or invalid.");
                }
            }
        }
        // Create a new directory if none were usable
        if (chosenDir == null) {
            chosenDir = Path.of(rootDir.toString(), "index-" + System.currentTimeMillis());
            Files.createDirectories(chosenDir);
            System.out.println("Created new directory: " + chosenDir);
        }
        // Open the chosen directory for indexing
        index = FSDirectory.open(chosenDir);
        analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(index, config);
    }

    @Override
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException {
        for (Text value : values) {
            Document doc = new Document();
            doc.add(new TextField("title", key.toString(), Field.Store.YES));
            doc.add(new StringField("text", value.toString(), Field.Store.YES));
            try {
                writer.addDocument(doc);
            } catch (IOException e) {
                context.getCounter("IndexerErrors", "addingDocumentError").increment(1);
                e.printStackTrace();
            }finally {
                cleanup(context);
            }
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException {
        analyzer.close();
        writer.close();
        index.close();
    }
}
