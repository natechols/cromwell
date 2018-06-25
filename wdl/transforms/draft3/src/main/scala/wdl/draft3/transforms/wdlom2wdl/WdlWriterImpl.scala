package wdl.draft3.transforms.wdlom2wdl

import wdl.model.draft3.elements.CommandPartElement.{PlaceholderCommandPartElement, StringCommandPartElement}
import wdl.model.draft3.elements.ExpressionElement._
import wdl.model.draft3.elements._
import wom.callable.MetaValueElement
import wom.callable.MetaValueElement._
import wom.types._

object WdlWriterImpl {

  // Auto-generated by simulacrum
  import WdlWriter.ops._

  implicit val ifWriter: WdlWriter[IfElement] = new WdlWriter[IfElement] {
    override def toWdlV1(a: IfElement) = {
      s"""if (${a.conditionExpression.toWdlV1}) {
         |${indentAndCombine(a.graphElements.map(_.toWdlV1))}}""".stripMargin
    }
  }

  implicit val stringPieceWriter: WdlWriter[StringPiece] = new WdlWriter[StringPiece] {
    override def toWdlV1(a: StringPiece): String = a match {
      case a: StringLiteral     => a.value
      case a: StringPlaceholder => "~{" + a.expr.toWdlV1 + "}"
    }
  }

  // Recursive references must be explicit
  implicit val expressionElementWriter: WdlWriter[ExpressionElement] = new WdlWriter[ExpressionElement] {
    override def toWdlV1(a: ExpressionElement) = a match {
      case a: PrimitiveLiteralExpressionElement => a.toWdlV1
      case a: StringExpression => "\"" + a.pieces.map(_.toWdlV1).mkString + "\""
      case a: StringLiteral => "\"" + a.value + "\""
      case a: ObjectLiteral =>
        "object { " + a.elements.map { pair =>
          pair._1 + ": " + expressionElementWriter.toWdlV1(pair._2)
        }.mkString(", ") + " }"
      case a: ArrayLiteral =>
        "[" + a.elements.map(expressionElementWriter.toWdlV1).mkString(", ") + "]"
      case a: MapLiteral =>
        "{ " + a.elements.map { pair =>
          expressionElementWriter.toWdlV1(pair._1) + ": " + expressionElementWriter.toWdlV1(pair._2)
        }.mkString(", ") + " }"
      case a: PairLiteral =>
        s"(${expressionElementWriter.toWdlV1(a.left)}, ${expressionElementWriter.toWdlV1(a.right)})"
      case a: UnaryOperation => a.toWdlV1
      case a: BinaryOperation => a.toWdlV1
      case a: TernaryIf =>
        s"if ${expressionElementWriter.toWdlV1(a.condition)} then ${expressionElementWriter.toWdlV1(a.ifTrue)} else ${expressionElementWriter.toWdlV1(a.ifFalse)}"
      case a: FunctionCallElement => a.toWdlV1
      case a: IdentifierLookup => a.identifier
      case a: IdentifierMemberAccess => a.toWdlV1
      case a: ExpressionMemberAccess => s"${expressionElementWriter.toWdlV1(a.expression)}.${a.memberAccessTail.toList.mkString(".")}"
      case a: IndexAccess => s"${expressionElementWriter.toWdlV1(a.expressionElement)}[${expressionElementWriter.toWdlV1(a.index)}]"
    }
  }

  implicit val unaryOperationWriter: WdlWriter[UnaryOperation] = new WdlWriter[UnaryOperation] {
    override def toWdlV1(a: UnaryOperation): String = a match {
      case a: LogicalNot    => s"!(${a.argument.toWdlV1})"
      case a: UnaryNegation => s"-(${a.argument.toWdlV1})"
      case a: UnaryPlus     => s"+(${a.argument.toWdlV1})"
    }
  }

  implicit val identifierMemberAccessWriter: WdlWriter[IdentifierMemberAccess] = new WdlWriter[IdentifierMemberAccess] {
    override def toWdlV1(a: IdentifierMemberAccess): String = {
      s"${a.first}.${a.second}" + (if (a.memberAccessTail.nonEmpty) {
        "." + a.memberAccessTail.mkString(".")
      } else {
        ""
      })
    }
  }

  implicit val binaryOperationWriter: WdlWriter[BinaryOperation] = new WdlWriter[BinaryOperation] {
    override def toWdlV1(a: BinaryOperation) = {
      val op = a match {
        case _: LogicalOr           => "||"
        case _: LogicalAnd          => "&&"
        case _: Equals              => "=="
        case _: NotEquals           => "!="
        case _: LessThan            => "<"
        case _: LessThanOrEquals    => "<="
        case _: GreaterThan         => ">"
        case _: GreaterThanOrEquals => ">="
        case _: Add                 => "+"
        case _: Subtract            => "-"
        case _: Multiply            => "*"
        case _: Divide              => "/"
        case _: Remainder           => "%"
      }

      s"(${a.left.toWdlV1} $op ${a.right.toWdlV1})"
    }
  }

  implicit val graphElementWriter: WdlWriter[WorkflowGraphElement] = new WdlWriter[WorkflowGraphElement] {
    override def toWdlV1(a: WorkflowGraphElement) = a match {
      case a: CallElement => a.toWdlV1
      case a: IntermediateValueDeclarationElement => a.toWdlV1
      case a: OutputDeclarationElement => a.toWdlV1
      case a: InputDeclarationElement => a.toWdlV1
      case a: IfElement => a.toWdlV1
      case a: ScatterElement => a.toWdlV1
    }
  }

  implicit val scatterElementWriter: WdlWriter[ScatterElement] = new WdlWriter[ScatterElement] {
    override def toWdlV1(a: ScatterElement): String =
      s"""scatter (${a.scatterVariableName} in ${a.scatterExpression.toWdlV1}) {
         |${indentAndCombine(a.graphElements.map(_.toWdlV1))}}""".stripMargin
  }

  implicit val callBodyElement: WdlWriter[CallBodyElement] = new WdlWriter[CallBodyElement] {
    override def toWdlV1(a: CallBodyElement): String = {
      if (a.inputs.nonEmpty) {
        s"""input:
           |${indent(indent(a.inputs.map(_.toWdlV1).mkString(", ")))}""".stripMargin
      } else {
        ""
      }
    }
  }

  implicit val callElementWriter: WdlWriter[CallElement] = new WdlWriter[CallElement] {
    override def toWdlV1(a: CallElement) = {
      val aliasExpression = a.alias match {
        case Some(alias) => s" as $alias"
        case None => ""
      }

      val bodyExpression = a.body match {
        case Some(body) =>
          s""" {
             |  ${body.toWdlV1}
             |}""".stripMargin
        case None => ""
      }

      s"call ${a.callableReference}$aliasExpression$bodyExpression"
    }
  }

  implicit val intermediateValueDeclarationElementWriter: WdlWriter[IntermediateValueDeclarationElement] = new WdlWriter[IntermediateValueDeclarationElement] {
    override def toWdlV1(a: IntermediateValueDeclarationElement) =
      s"${a.typeElement.toWdlV1} ${a.name} = ${a.expression.toWdlV1}"
  }

  implicit val typeElementWriter: WdlWriter[TypeElement] = new WdlWriter[TypeElement] {
    override def toWdlV1(a: TypeElement) = a match {
      case a: PrimitiveTypeElement => a.primitiveType.toWdlV1
      case a: ArrayTypeElement => s"Array[${typeElementWriter.toWdlV1(a.inner)}]"
      case a: MapTypeElement => s"Map[${typeElementWriter.toWdlV1(a.keyType)}, ${typeElementWriter.toWdlV1(a.valueType)}]"
      case a: OptionalTypeElement => s"${typeElementWriter.toWdlV1(a.maybeType)}?"
      case a: NonEmptyTypeElement => s"${typeElementWriter.toWdlV1(a.arrayType)}+"
      case a: PairTypeElement => s"Pair[${typeElementWriter.toWdlV1(a.leftType)}, ${typeElementWriter.toWdlV1(a.rightType)}]"
      case _: ObjectTypeElement.type => "Object"
      case a: TypeAliasElement => a.alias
    }
  }

  implicit val primitiveTypeElementWriter: WdlWriter[WomPrimitiveType] = new WdlWriter[WomPrimitiveType] {
    override def toWdlV1(a: WomPrimitiveType) = a.toDisplayString
  }

  implicit val workflowDefinitionElementWriter: WdlWriter[WorkflowDefinitionElement] = new WdlWriter[WorkflowDefinitionElement] {
    override def toWdlV1(a: WorkflowDefinitionElement) = {
      val inputs = a.inputsSection match {
        case Some(i) => i.toWdlV1
        case None => ""
      }
      val outputs = a.outputsSection match {
        case Some(o) => o.toWdlV1
        case None => ""
      }

      s"""workflow ${a.name} {
         |${indent(inputs)}
         |${indentAndCombine(a.graphElements.map(_.toWdlV1))}
         |${indent(outputs)}
         |}""".stripMargin
    }
  }

  implicit val runtimeAttributesSectionElementWriter: WdlWriter[RuntimeAttributesSectionElement] = new WdlWriter[RuntimeAttributesSectionElement] {
    override def toWdlV1(a: RuntimeAttributesSectionElement): String = {
      val runtimeMap = a.runtimeAttributes map { pair =>
        s"${pair.key}: ${pair.value.toWdlV1}"
      }

      s"""runtime {
         |${indentAndCombine(runtimeMap)}}""".stripMargin
    }
  }

  implicit val metaValueElementWriter: WdlWriter[MetaValueElement] = new WdlWriter[MetaValueElement] {
    override def toWdlV1(a: MetaValueElement): String = a match {
      case _: MetaValueElementNull.type => "null"
      case a: MetaValueElementBoolean => a.value.toString
      case a: MetaValueElementFloat => a.value.toString
      case a: MetaValueElementInteger => a.value.toString
      case a: MetaValueElementString => "\"" + a.value + "\""
      case a: MetaValueElementObject =>
        "{" + a.value.map { pair =>
          s"${pair._1}: ${metaValueElementWriter.toWdlV1(pair._2)}"
        }.mkString(", ") + "}"
      case a: MetaValueElementArray => "[" + a.value.map(metaValueElementWriter.toWdlV1).mkString(", ") + "]"
    }
  }


  implicit val metaSectionElementWriter: WdlWriter[MetaSectionElement] = new WdlWriter[MetaSectionElement] {
    override def toWdlV1(a: MetaSectionElement): String = {
      val map = a.meta.map { pair =>
        s"${pair._1}: ${pair._2.toWdlV1}"
      }
      s"""meta {
         |${indentAndCombine(map)}
         |}""".stripMargin
    }
  }

  implicit val parameterMetaSectionElementWriter: WdlWriter[ParameterMetaSectionElement] = new WdlWriter[ParameterMetaSectionElement] {
    override def toWdlV1(a: ParameterMetaSectionElement): String = {
      val map = a.metaAttributes.map { pair =>
        s"${pair._1}: ${pair._2.toWdlV1}"
      }
      s"""parameter_meta {
         |${indentAndCombine(map)}
         |}""".stripMargin
    }
  }

  implicit val taskDefinitionTypeElementWriter: WdlWriter[TaskDefinitionElement] = new WdlWriter[TaskDefinitionElement] {
    override def toWdlV1(a: TaskDefinitionElement) = {
      val inputs = a.inputsSection match {
        case Some(i) => i.toWdlV1
        case None => ""
      }
      val outputs = a.outputsSection match {
        case Some(o) => o.toWdlV1
        case None => ""
      }
      val runtime = a.runtimeSection match {
        case Some(r) => r.toWdlV1
        case None => ""
      }
      val meta = a.metaSection match {
        case Some(m) => m.toWdlV1
        case None => ""
      }
      val parameterMeta = a.parameterMetaSection match {
        case Some(p) => p.toWdlV1
        case None => ""
      }

      s"""task ${a.name} {
         |${indent(inputs)}
         |${indentAndCombine(a.declarations.map(_.toWdlV1))}
         |${indent(outputs)}
         |${a.commandSection.toWdlV1}
         |${indent(runtime)}
         |${indent(meta)}
         |${indent(parameterMeta)}}""".stripMargin
    }
  }

  implicit val commandSectionElementWriter: WdlWriter[CommandSectionElement] = new WdlWriter[CommandSectionElement] {
    override def toWdlV1(a: CommandSectionElement): String = {
      s"""command <<<
         |${combine(a.parts.map(_.toWdlV1))}>>>""".stripMargin
    }
  }

  implicit val commandSectionLineWriter: WdlWriter[CommandSectionLine] = new WdlWriter[CommandSectionLine] {
    override def toWdlV1(a: CommandSectionLine): String = {
      a.parts.map(_.toWdlV1).mkString
    }
  }

  implicit val commandPartElementWriter: WdlWriter[CommandPartElement] = new WdlWriter[CommandPartElement] {
    override def toWdlV1(a: CommandPartElement): String = a match {
      case a: StringCommandPartElement => a.value // .trim?
      case a: PlaceholderCommandPartElement =>
        val attributes = a.attributes.toWdlV1

        if (attributes.nonEmpty)
          s"~{$attributes ${a.expressionElement.toWdlV1}}"
        else
          s"~{${a.expressionElement.toWdlV1}}"
    }
  }

  implicit val placeholderAttributeSetWriter: WdlWriter[PlaceholderAttributeSet] = new WdlWriter[PlaceholderAttributeSet] {
    override def toWdlV1(a: PlaceholderAttributeSet): String = {
      val attrStrings = Map(
        "sep" -> a.sepAttribute,
        "true" -> a.trueAttribute,
        "false" -> a.falseAttribute,
        "default" -> a.defaultAttribute
      ).collect({ case (attrKey: String, Some(value)) => attrKey + "=\"" + value + "\"" })

      if (attrStrings.isEmpty) "" else attrStrings.mkString(start = "", sep = " ", end = " ")
    }
  }

  implicit val inputsSectionElementWriter: WdlWriter[InputsSectionElement] = new WdlWriter[InputsSectionElement] {
    override def toWdlV1(a: InputsSectionElement): String = {
      s"""input {
         |${indentAndCombine(a.inputDeclarations.map(_.toWdlV1))}}""".stripMargin
    }
  }

  implicit val inputDeclarationElementWriter: WdlWriter[InputDeclarationElement] = new WdlWriter[InputDeclarationElement] {
    override def toWdlV1(a: InputDeclarationElement): String = {
      val expression = a.expression match {
        case Some(expr) => s" = ${expr.toWdlV1}"
        case None => ""
      }

      s"${a.typeElement.toWdlV1} ${a.name}$expression"
    }
  }

  implicit val outputsSectionElementWriter: WdlWriter[OutputsSectionElement] = new WdlWriter[OutputsSectionElement] {
    override def toWdlV1(a: OutputsSectionElement): String = {
      s"""output {
         |${indentAndCombine(a.outputs.map(_.toWdlV1))}}""".stripMargin
    }
  }

  implicit val outputDeclarationElementWriter: WdlWriter[OutputDeclarationElement] = new WdlWriter[OutputDeclarationElement] {
    override def toWdlV1(a: OutputDeclarationElement): String = {
      s"${a.typeElement.toWdlV1} ${a.name} = ${a.expression.toWdlV1}"
    }
  }

  implicit val functionCallElementWriter: WdlWriter[FunctionCallElement] = new WdlWriter[FunctionCallElement] {
    override def toWdlV1(a: FunctionCallElement): String = a match {
      case _: StdoutElement.type => "stdout()"
      case _: StderrElement.type => "stderr()"
      case a: OneParamFunctionCallElement => a.toWdlV1
      case a: OneOrTwoParamFunctionCallElement => a.toWdlV1
      case a: TwoParamFunctionCallElement => a.toWdlV1
      case a: Sub => s"sub(${a.input.toWdlV1}, ${a.pattern.toWdlV1}, ${a.replace.toWdlV1})"
    }
  }

  implicit val oneParamFunctionCallElementWriter: WdlWriter[OneParamFunctionCallElement] = new WdlWriter[OneParamFunctionCallElement] {
    override def toWdlV1(a: OneParamFunctionCallElement): String = {
      val fn = a match {
        case _: ReadLines    => "read_lines"
        case _: ReadTsv      => "read_tsv"
        case _: ReadMap      => "read_map"
        case _: ReadObject   => "read_object"
        case _: ReadObjects  => "read_objects"
        case _: ReadJson     => "read_json"
        case _: ReadInt      => "read_int"
        case _: ReadString   => "read_string"
        case _: ReadFloat    => "read_float"
        case _: ReadBoolean  => "read_boolean"
        case _: WriteLines   => "write_lines"
        case _: WriteTsv     => "write_tsv"
        case _: WriteMap     => "write_map"
        case _: WriteObject  => "write_object"
        case _: WriteObjects => "write_objects"
        case _: WriteJson    => "write_json"
        case _: Range        => "range"
        case _: Transpose    => "transpose"
        case _: Length       => "length"
        case _: Flatten      => "flatten"
        case _: SelectFirst  => "select_first"
        case _: SelectAll    => "select_all"
        case _: Defined      => "defined"
        case _: Floor        => "floor"
        case _: Ceil         => "ceil"
        case _: Round        => "round"
        case _: Glob         => "glob"
      }

      s"$fn(${a.param.toWdlV1})"
    }
  }

  implicit val oneOrTwoParamFunctionCallElementWriter: WdlWriter[OneOrTwoParamFunctionCallElement] = new WdlWriter[OneOrTwoParamFunctionCallElement] {
    override def toWdlV1(a: OneOrTwoParamFunctionCallElement): String = {
      (a, a.secondParam) match {
        case (_: Size, Some(unit)) => s"size(${a.firstParam.toWdlV1}, ${unit.toWdlV1})"
        case (_: Size, None) => s"size(${a.firstParam.toWdlV1})"
        case (_: Basename, Some(suffix)) => s"basename(${a.firstParam.toWdlV1}, ${suffix.toWdlV1})"
        case (_: Basename, None) => s"basename(${a.firstParam.toWdlV1})"
      }
    }
  }

  implicit val twoParamFunctionCallElementWriter: WdlWriter[TwoParamFunctionCallElement] = new WdlWriter[TwoParamFunctionCallElement] {
    override def toWdlV1(a: TwoParamFunctionCallElement): String = {
      def functionCall(name: String) = s"$name(${a.arg1.toWdlV1}, ${a.arg2.toWdlV1})"

      a match {
        case _: Zip => functionCall("zip")
        case _: Cross => functionCall("cross")
        case _: Prefix => functionCall("prefix")
      }
    }
  }

  implicit val structElementWriter: WdlWriter[StructElement] = new WdlWriter[StructElement] {
    override def toWdlV1(a: StructElement): String =
      s"""struct ${a.name} {
         |${indentAndCombine(a.entries.map(_.toWdlV1))}}""".stripMargin
  }

  implicit val structEntryElementWriter: WdlWriter[StructEntryElement] = new WdlWriter[StructEntryElement] {
    override def toWdlV1(a: StructEntryElement): String = s"${a.typeElement.toWdlV1} ${a.identifier}"
  }

  implicit val fileElementWriter: WdlWriter[FileElement] = new WdlWriter[FileElement] {
    override def toWdlV1(a: FileElement) = {
      "version 1.0" +
      combine(a.structs.map(_.toWdlV1)) +
      combine(a.tasks.map(_.toWdlV1)) +
      combine(a.workflows.map(_.toWdlV1))
    }
  }

  implicit val kvPairWriter: WdlWriter[KvPair] = new WdlWriter[KvPair] {
    override def toWdlV1(a: KvPair): String = s"${a.key} = ${a.value.toWdlV1}"
  }

  implicit val primitiveLiteralExpressionElementWriter: WdlWriter[PrimitiveLiteralExpressionElement] = new WdlWriter[PrimitiveLiteralExpressionElement] {
    override def toWdlV1(a: PrimitiveLiteralExpressionElement) = a.value.toWomString
  }
}
