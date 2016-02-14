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
package org.walkmod.git.constraints;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.constraints.NodesPerLineConstraint;
import org.walkmod.modelchecker.Constraint;
import org.walkmod.modelchecker.ConstraintProvider;

public class JavaConstraintProvider implements ConstraintProvider<CompilationUnit> {

   private Map<String, DiffEntry> diffEntries = new HashMap<String, DiffEntry>();

   private WalkmodDiffFormatter formatter;

   public JavaConstraintProvider() throws IOException {
      File file = new File(".git").getCanonicalFile();
      if (file.exists()) {
         Git git = Git.open(file.getAbsoluteFile().getParentFile().getCanonicalFile());

         formatter = new WalkmodDiffFormatter(git.getRepository());

         AbstractTreeIterator commitTreeIterator = prepareTreeParser(git.getRepository(), Constants.HEAD);
         FileTreeIterator workTreeIterator = new FileTreeIterator(git.getRepository());
         List<DiffEntry> diffEntries = formatter.scan(commitTreeIterator, workTreeIterator);

         for (DiffEntry entry : diffEntries) {
            File aux = new File(entry.getNewPath()).getCanonicalFile();
            this.diffEntries.put(aux.getPath(), entry);
         }
         formatter.close();
      }
   }

   private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId)
         throws IOException, MissingObjectException, IncorrectObjectTypeException {
      // from the commit we can build the tree which allows us to construct the TreeParser
      RevWalk walk = new RevWalk(repository);
      ObjectId oid = repository.resolve(objectId);
      RevCommit commit = walk.parseCommit(oid);
      RevTree tree = walk.parseTree(commit.getTree().getId());

      CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
      ObjectReader oldReader = repository.newObjectReader();
      oldTreeParser.reset(oldReader, tree.getId());

      walk.dispose();
      walk.close();
      oldReader.close();
      return oldTreeParser;
   }

   @Override
   public Constraint<?> getConstraint(CompilationUnit model) {
      URI uri = model.getURI();
      if (uri != null) {
         File aux = new File(uri);
         DiffEntry ent = diffEntries.get(aux.getPath());
         if (ent == null) {
            return null;
         } else {
            NodesPerLineConstraint constraint = new NodesPerLineConstraint();
            try {
               List<Integer> lines = formatter.getLines(ent);
               for (Integer line : lines) {
                  constraint.addLine(line);
               }
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
            return constraint;
         }
      }
      return null;
   }

}
