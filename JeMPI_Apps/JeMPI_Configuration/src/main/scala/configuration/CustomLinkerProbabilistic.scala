package configuration

import java.io.{File, PrintWriter}
import scala.language.{existentials, postfixOps}


object CustomLinkerProbabilistic {

  private val classLocation = "../JeMPI_LibShared/src/main/java/org/jembi/jempi/linker"
  private val custom_className = "CustomLinkerProbabilistic"
  private val packageText = "org.jembi.jempi.linker"

  def parseRules(config: Config): Any = {
    val classFile: String = classLocation + File.separator + custom_className + ".java"
    println("Creating " + classFile)
    val file: File = new File(classFile)
    val writer: PrintWriter = new PrintWriter(file)

    val muList = for (
      t <- config.commonFields.filter(f => f.m.isDefined && f.u.isDefined)
    ) yield t

    writer.println(s"""package $packageText;""")
    if (muList.length == 0) {
      writer.println()
      writer.println(
        s"""
           |import org.jembi.jempi.shared.models.CustomMU;
           |import org.jembi.jempi.shared.models.CustomDemographicData;
           |
           |public final class $custom_className {
           |
           |  private $custom_className() {
           |  }
           |
           |  public static float probabilisticScore(final CustomDemographicData goldenRecord,
           |                                         final CustomDemographicData interaction) {
           |    return 0.0F;
           |  }
           |
           |  public static void updateMU(final CustomMU mu) {
           |  }
           |
           |  public static void checkUpdatedMU() {
           |  }
           |
           |  static CustomMU getMU() {
           |    return new CustomMU(null);
           |  }
           |
           |}""".stripMargin)
    } else {
      writer.println(
        s"""
           |import org.apache.commons.text.similarity.JaroWinklerSimilarity;
           |import org.jembi.jempi.shared.models.CustomDemographicData;
           |import org.jembi.jempi.shared.models.CustomMU;
           |
           |public final class $custom_className {
           |
           |   private static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();
           |
           |   static Fields updatedFields = null;
           |
           |   private $custom_className() {
           |   }
           |""".stripMargin)

      writer.println("   static CustomMU getMU() {")
      writer.println("      return new CustomMU(")
      muList.zipWithIndex.foreach((mu, idx) => {
        writer.print(" " * 9 + s"LinkerProbabilistic.getProbability(currentFields.${Utils.snakeCaseToCamelCase(mu.fieldName)})")
        if (idx + 1 < muList.length)
          writer.println(",")
        else
          writer.println(
            """);
              |   }
              |""".stripMargin)
      })


      writer.println("   private record Fields(")
      muList.zipWithIndex.foreach((mu, idx) => {
        writer.print(s"""${" " * 9}LinkerProbabilistic.Field """)
        writer.print(Utils.snakeCaseToCamelCase(mu.fieldName))
        if (idx + 1 < muList.length)
          writer.println(",")
        else
          writer.println(
            s""") {
               |   }""".stripMargin)
          writer.println()
        end if
      })

      writer.println("   static Fields currentFields =")
      writer.print("      new Fields(")
      var margin = 0
      muList.zipWithIndex.foreach((field, idx) => {
        val m: Double = field.m.get
        val u: Double = field.u.get
        writer.print(" " * margin + s"new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, ${m}F, ${u}F)")
        if (idx + 1 < muList.length)
          writer.println(",")
          margin = 17
        else
          writer.println(");")
      })
      writer.println()
      writer.println(
        """   public static float probabilisticScore(
          |         final CustomDemographicData goldenRecord,
          |         final CustomDemographicData interaction) {
          |      // min, max, score, missingPenalty
          |      final float[] metrics = {0, 0, 0, 1.0F};""".stripMargin)
      muList.zipWithIndex.foreach((field, _) => {
        writer.println(" " * 6 + "LinkerProbabilistic.updateMetricsForStringField(metrics,")
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.println(" " * 54 + s"goldenRecord.$fieldName, interaction.$fieldName, currentFields" +
          s".$fieldName);")
      })
      writer.println(" " * 6 + "return ((metrics[2] - metrics[0]) / (metrics[1] - metrics[0])) * metrics[3];")
      writer.println(" " * 3 + "}")
      writer.println()
      writer.println("   public static void updateMU(final CustomMU mu) {")
      muList.zipWithIndex.foreach((field, idx) => {
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        if (idx == 0)
          writer.print(" " * 6 + s"if (mu.$fieldName().m() > mu.$fieldName().u()")
        else
          writer.print(" " * 10 + s"&& mu.$fieldName().m() > mu.$fieldName().u()")
        end if
        if (idx + 1 < muList.length)
          writer.println()
        else
          writer.println(") {")
        end if
      })
      writer.println(" " * 9 + "updatedFields = new Fields(")
      muList.zipWithIndex.foreach((field, idx) => {
        val fieldName = Utils.snakeCaseToCamelCase(field.fieldName)
        writer.print(" " * 12 + s"new LinkerProbabilistic.Field(JARO_WINKLER_SIMILARITY, mu.$fieldName().m(), mu.$fieldName().u())")
        if (idx + 1 < muList.length)
          writer.println(",")
        else
          writer.println(");")
        end if

      })
      writer.println(" " * 6 + "}")
      writer.println("   }")
      writer.println()
      writer.println("}")
    }
    writer.flush()
    writer.close()
  }

}
