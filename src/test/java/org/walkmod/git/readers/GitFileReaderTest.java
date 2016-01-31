package org.walkmod.git.readers;

import java.io.File;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.Resource;

public class GitFileReaderTest {

   @Test
   public void testUnstashed() throws Exception {
      GitFileReader reader = new GitFileReader();
      reader.setPath("src/main/java");
      File tempFile = new File("src/main/java/hello.txt");
      if (tempFile.exists()) {
         tempFile.delete();
      }
      if (tempFile.createNewFile()) {
         try {
            Resource<File> files = reader.read();
            Iterator<File> it = files.iterator();
            boolean contains = false;
            while (it.hasNext() && !contains) {
               File current = it.next();
               contains = current.getName().equals("hello.txt");
            }
            Assert.assertTrue(contains);
         } finally {
            tempFile.delete();
         }
      }
   }

   @Test
   public void testLastCommit() throws Exception {
      GitFileReader reader = new GitFileReader();
      reader.setPath("src/main/java");

      Resource<File> files = reader.read();
      Iterator<File> it = files.iterator();
      boolean contains = false;
      while (it.hasNext() && !contains) {
         File current = it.next();
         contains = current.getName().contains("GitFileReader");
      }
      Assert.assertTrue(contains);

   }
}
