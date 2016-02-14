package org.walkmod.git.constraints;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.constraints.NodesPerLineConstraint;
import org.walkmod.modelchecker.Constraint;

public class JavaConstraintsProviderTest {

   @Test
   public void test() throws Exception {
      JavaConstraintProvider jcp = new JavaConstraintProvider();
      File dir = new File("src/test/resources");
      dir.mkdirs();
      File file = new File("src/test/resources/Foo.java");
      if(file.exists()){
         file.delete();
      }
      file.createNewFile();
      FileUtils.write(file, "public class Foo {}");
      CompilationUnit cu = ASTManager.parse(file);
      Constraint<?> c = jcp.getConstraint(cu);
      Assert.assertTrue(c instanceof NodesPerLineConstraint);

      file.delete();
   }
}
