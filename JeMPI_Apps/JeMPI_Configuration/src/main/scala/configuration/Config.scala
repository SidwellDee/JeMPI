package configuration

case class CommonField(fieldName: String,
                       fieldType: String,
                       isList: Option[Boolean],
                       indexGoldenRecord: Option[String],
                       indexEntity: Option[String],
                       comparison: Option[String],
                       comparisonLevel: Option[Double],
                       m: Option[Double],
                       u: Option[Double])

case class Rule(vars: Array[String],
                text: String)

case class Rules(deterministic: Map[String, Rule],
                 probabilistic: Map[String, Rule])

case class Config(commonFields: Array[CommonField],
                  rules: Rules)


