/* 
  Copyright (C) 2016 Raquel Pau.
 
 Walkmod is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Walkmod is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/
package org.walkmod.git.readers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.walkmod.Resource;
import org.walkmod.readers.DefaultFileReader;

public class GitFileReader extends DefaultFileReader {

   @Override
   public Resource<File> read() throws Exception {
      File file = new File(".git");
      Resource<File> result = null;
      if (file.exists()) {

         Git git = Git.open(file.getAbsoluteFile().getParentFile().getCanonicalFile());

         try {
            StatusCommand cmd = git.status();
            List<String> cfgIncludesList = null;
            String[] cfgIncludes = getIncludes();
            if (cfgIncludes != null && cfgIncludes.length > 0) {
               cfgIncludesList = Arrays.asList(cfgIncludes);
            }
            String path = getPath();
            Status status = cmd.call();
            Set<String> uncommitted = status.getUncommittedChanges();
            uncommitted.addAll(status.getUntracked());
            Set<String> includes = new HashSet<String>();
            if (!uncommitted.isEmpty()) {

               for (String uncommittedFile : uncommitted) {
                  if (uncommittedFile.startsWith(path)) {
                     String fileName = uncommittedFile.substring(path.length() + 1);
                     if (cfgIncludesList == null || cfgIncludesList.contains(fileName)) {
                        includes.add(fileName);
                     }
                  }
               }

              
            } else {

               Set<String> filesInCommit = getFilesInHEAD(git.getRepository());
               for (String committedFile : filesInCommit) {
                  if (committedFile.startsWith(path)) {
                     String fileName = committedFile.substring(path.length() + 1);
                     if (cfgIncludesList == null || cfgIncludesList.contains(fileName)) {
                        includes.add(fileName);
                     }
                  }
               }

            }
            if (!includes.isEmpty()) {
               String[] includesArray = new String[includes.size()];
               includesArray = includes.toArray(includesArray);

               setIncludes(includesArray);
            }
         } finally {
            git.close();
         }
      }
      result = super.read();
      return result;
   }

   /**
    * Returns the list of files changed in the last commit.
    *
    * @param repository
    * @return list of files changed in a commit
    * @throws IOException
    * @throws IncorrectObjectTypeException
    * @throws AmbiguousObjectException
    * @throws RevisionSyntaxException
    */
   private static Set<String> getFilesInHEAD(Repository repository)
         throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
      Set<String> list = new HashSet<String>();

      RevWalk rw = new RevWalk(repository);
      ObjectId oid = repository.resolve("HEAD");
      RevCommit commit = rw.parseCommit(oid);

      try {

         if (commit.getParentCount() == 0) {
            TreeWalk tw = new TreeWalk(repository);
            tw.reset();
            tw.setRecursive(true);
            tw.addTree(commit.getTree());
            while (tw.next()) {

               list.add(tw.getPathString());
            }
            tw.close();

         }
      } catch (Throwable t) {
         throw new RuntimeException("failed to determine files in commit!");
      } finally {
         rw.dispose();
         rw.close();
      }
      return list;
   }

}
