package org.walkmod.git.readers;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.walkmod.Resource;
import org.walkmod.readers.DefaultFileReader;

public class GitFileReader extends DefaultFileReader {

   @Override
   public Resource<File> read() throws Exception {
      File file = new File(".git");
      Resource<File> result = null;
      if (file.exists()) {
         FileRepositoryBuilder builder = new FileRepositoryBuilder();
         Repository repository = builder.setGitDir(file.getAbsoluteFile().getParentFile().getCanonicalFile()).readEnvironment() // scan environment GIT_* variables
               .findGitDir() // scan up the file system tree
               .build();
         Git git = new Git(repository);
         try {
            StatusCommand cmd = git.status();

            Status status = cmd.call();
            Set<String> uncommitted = status.getUncommittedChanges();
            Set<String> includes = new HashSet<String>();
            if (!uncommitted.isEmpty()) {

               String path = getPath();

               for (String uncommittedFile : uncommitted) {
                  if (uncommittedFile.startsWith(path)) {
                     includes.add(uncommittedFile.substring(path.length()));
                  }
               }

            } else {
               
            }
            if (!includes.isEmpty()) {
               String[] includesArray = new String[includes.size()];
               setIncludes(includes.toArray(includesArray));
            }
         } finally {
            git.close();
         }
      }
      result = super.read();
      return result;
   }
}
