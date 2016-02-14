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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;

public class WalkmodDiffFormatter {

   private ByteArrayOutputStream os;

   private DiffFormatter df;

   public WalkmodDiffFormatter(Repository repo) {
      this.os = new ByteArrayOutputStream();
      df = new DiffFormatter(os);

      df.setRepository(repo);

   }

   public List<DiffEntry> scan(AbstractTreeIterator a, AbstractTreeIterator b) throws IOException {
      return df.scan(a, b);
   }

   public void close() throws IOException {
      df.close();
   }

   public List<Integer> getLines(DiffEntry ent) throws IOException {
      df.format(ent);
      String content = os.toString();
      String[] lines = content.split(System.getProperty("line.separator"));
      List<Integer> result = new LinkedList<Integer>();
      for (int i = 0; i < lines.length; i++) {
         if (lines[i].startsWith("@@ ")) {
            int lastIndex = lines[i].indexOf(" @@");
            if (lastIndex != -1) {
               String changes = lines[i].substring(3, lastIndex);
               String[] parts = changes.split(" ");
               if (parts[1].startsWith("+")) {
                  String[] numbersOld = parts[0].substring(1).split(",");
                  String[] numbersNew = parts[1].substring(1).split(",");
                  Integer begin = Integer.parseInt(numbersOld[numbersOld.length - 1]);
                  Integer end = Integer.parseInt(numbersNew[numbersNew.length - 1]);
                  for (int j = begin; j <= end; j++) {
                     result.add(j+1);
                  }
               }
            }
         }
      }
      os.reset();
      return result;
   }

}
