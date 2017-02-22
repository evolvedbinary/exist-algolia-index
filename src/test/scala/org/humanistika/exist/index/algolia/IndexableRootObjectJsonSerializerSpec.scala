package org.humanistika.exist.index.algolia

import java.io.{ByteArrayInputStream, StringWriter}
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory

import com.fasterxml.jackson.databind.ObjectMapper
import org.specs2.Specification
import org.w3c.dom.{Attr, Document, Element, Node}

import scalaz._
import Scalaz._
import resource._

import scala.util.{Success, Failure}

class IndexableRootObjectJsonSerializerSpec extends Specification { def is = s2"""
  This is a specification to check the JSON Serialization of IndexableRootObject

    The basic JSON serialized result must
      have a document id $e1
      prefer the user specified document id $e2
      have a nodeId (if provided) $e3
      prefer the user specified node id $e4

    The JSON serialized result attributes for DOM Attributes must
      be constructable $e4
      be float convertible $e5
      be int convertible $e6
      be boolean convertible $e7
      allow multiple $e8
      support arrays $e9

    The JSON serialized result attributes for DOM Elements must
      be constructable $e10
      be float convertible $e11
      be int convertible $e12
      be boolean convertible $e13
      allow multiple $e14
      serialize all text nodes $e15
      support arrays $e16

    The JSON serialized result objects for DOM Attributes must
      be the same as a result attribute $e17
      support arrays $e18

    The JSON serialized result objects for DOM Elements must
      be constructable $e19
      write nested elements $e20
      write array $e21
      write nested array $e22
      support arrays $e23

  """

  def e1 = {
    val indexableRootObject = IndexableRootObject("/db/a1", 5, 46, None, None, None, Seq.empty)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"5/46/0","collection":"/db/a1","documentID":46}"""
  }

  def e2 = {
    val indexableRootObject = IndexableRootObject("/db/a1", 5, 46, Some("my-document-id"), None, None, Seq.empty)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"5/46/0","collection":"/db/a1","documentID":"my-document-id"}"""
  }

  def e3 = {
    val indexableRootObject = IndexableRootObject("/db/a1", 6, 47, None, Some("1.2.2"), None, Seq.empty)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"6/47/1.2.2","collection":"/db/a1","documentID":47}"""
  }

  def e4 = {
    val indexableRootObject = IndexableRootObject("/db/a1", 5, 46, None, None, Some("my-node-id"), Seq.empty)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"my-node-id","collection":"/db/a1","documentID":46}"""
  }

  def e5 = {
    val attr1 = attr(dom("""<w value="hello"/>"""), "value")
    val attributes = Seq(-\/(IndexableAttribute("attr1", Seq(IndexableValue("1.1", \/-(attr1))), LiteralTypeConfig.String)))
    val indexableRootObject = IndexableRootObject("/db/a1", 7, 48, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"7/48/1","collection":"/db/a1","documentID":48,"attr1":"hello"}"""
  }

  def e6 = {
    val attr1 = attr(dom("""<x value="99.9"/>"""), "value")
    val attributes = Seq(-\/(IndexableAttribute("attr1", Seq(IndexableValue("1.1", \/-(attr1))), LiteralTypeConfig.Float)))
    val indexableRootObject = IndexableRootObject("/db/a1", 2, 49, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"2/49/1","collection":"/db/a1","documentID":49,"attr1":99.9}"""
  }

  def e7 = {
    val attr1 = attr(dom("""<y value="1012"/>"""), "value")
    val attributes = Seq(-\/(IndexableAttribute("attr1", Seq(IndexableValue("1.1", \/-(attr1))), LiteralTypeConfig.Integer)))
    val indexableRootObject = IndexableRootObject("/db/a1", 9, 50, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"9/50/1","collection":"/db/a1","documentID":50,"attr1":1012}"""
  }

  def e8 = {
    val attr1 = attr(dom("""<z value="true"/>"""), "value")
    val attributes = Seq(-\/(IndexableAttribute("attr1", Seq(IndexableValue("1.1", \/-(attr1))), LiteralTypeConfig.Boolean)))
    val indexableRootObject = IndexableRootObject("/db/a1", 3, 51, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"3/51/1","collection":"/db/a1","documentID":51,"attr1":true}"""
  }

  def e9 = {
    val dom1 = dom("""<pos x="99.9" y="11.4"/>""")
    val attr1 = attr(dom1, "x")
    val attr2 = attr(dom1, "y")
    val attributes = Seq(-\/(IndexableAttribute("attr1", Seq(IndexableValue("1.1", \/-(attr1))), LiteralTypeConfig.Float)), -\/(IndexableAttribute("attr2", Seq(IndexableValue("1.2", \/-(attr2))), LiteralTypeConfig.Float)))
    val indexableRootObject = IndexableRootObject("/db/a1", 3, 52, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"3/52/1","collection":"/db/a1","documentID":52,"attr1":99.9,"attr2":11.4}"""
  }

  def e10 = {
    val dom1 = dom("""<loc><pos x="99.9" y="11.4"/><pos x="202.2" y="10.2"/></loc>""")
    val pos = elems(dom1, "pos")
    val attr1_1 = attr(pos(0), "x")
    val attr1_2 = attr(pos(1), "x")
    val attr2_1 = attr(pos(0), "y")
    val attr2_2 = attr(pos(1), "y")
    val attributes = Seq(
      -\/(IndexableAttribute("xx", Seq(IndexableValue("1.1", \/-(attr1_1)), IndexableValue("2.1", \/-(attr1_2))), LiteralTypeConfig.Float)),
      -\/(IndexableAttribute("yy", Seq(IndexableValue("1.2", \/-(attr2_1)), IndexableValue("2.2", \/-(attr2_2))), LiteralTypeConfig.Float))
    )
    val indexableRootObject = IndexableRootObject("/db/a1", 7, 42, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"7/42/1","collection":"/db/a1","documentID":42,"xx":[99.9,202.2],"yy":[11.4,10.2]}"""
  }

  def e11 = {
    val elem1 = elem(dom("""<w>hello</w>"""), "w")
    val attributes = Seq(-\/(IndexableAttribute("elem1", Seq(IndexableValue("1.1", -\/(elem1))), LiteralTypeConfig.String)))
    val indexableRootObject = IndexableRootObject("/db/a1", 6, 48, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"6/48/1","collection":"/db/a1","documentID":48,"elem1":"hello"}"""
  }

  def e12 = {
    val elem1 = elem(dom("""<x>99.9</x>"""), "x")
    val attributes = Seq(-\/(IndexableAttribute("elem1", Seq(IndexableValue("1.1", -\/(elem1))), LiteralTypeConfig.Float)))
    val indexableRootObject = IndexableRootObject("/db/a1", 7, 48, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"7/48/1","collection":"/db/a1","documentID":48,"elem1":99.9}"""
  }

  def e13 = {
    val elem1 = elem(dom("""<y>1012</y>"""), "y")
    val attributes = Seq(-\/(IndexableAttribute("elem1", Seq(IndexableValue("1.1", -\/(elem1))), LiteralTypeConfig.Integer)))
    val indexableRootObject = IndexableRootObject("/db/a1", 2, 48, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"2/48/1","collection":"/db/a1","documentID":48,"elem1":1012}"""
  }

  def e14 = {
    val elem1 = elem(dom("""<z>true</z>"""), "z")
    val attributes = Seq(-\/(IndexableAttribute("elem1", Seq(IndexableValue("1.1", -\/(elem1))), LiteralTypeConfig.Boolean)))
    val indexableRootObject = IndexableRootObject("/db/a1", 1, 48, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"1/48/1","collection":"/db/a1","documentID":48,"elem1":true}"""
  }

  def e15 = {
    val elem1 = elem(dom("""<x>99.9</x>"""), "x")
    val elem2 = elem(dom("""<y>11.3</y>"""), "y")
    val attributes = Seq(-\/(IndexableAttribute("elem1", Seq(IndexableValue("1.1", -\/(elem1))), LiteralTypeConfig.Float)), -\/(IndexableAttribute("elem2", Seq(IndexableValue("1.2", -\/(elem2))), LiteralTypeConfig.Float)))
    val indexableRootObject = IndexableRootObject("/db/a1", 7, 48, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"7/48/1","collection":"/db/a1","documentID":48,"elem1":99.9,"elem2":11.3}"""
  }

  def e16 = {
    val elem1 = elem(dom("""<x>hello <b>world</b></x>"""), "x")
    val attributes = Seq(-\/(IndexableAttribute("elem1", Seq(IndexableValue("1.1", -\/(elem1))), LiteralTypeConfig.String)))
    val indexableRootObject = IndexableRootObject("/db/a1", 23, 48, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"23/48/1","collection":"/db/a1","documentID":48,"elem1":"hello world"}"""
  }

  def e17 = {
    val dom1 = dom("""<loc><pos><x>123.4</x><y>-17.45</y></pos><pos><x>456.12</x><y>15.67</y></pos></loc>""")
    val pos = elems(dom1, "pos")
    val elem1_1 = childElem(pos(0), "x")
    val elem1_2 = childElem(pos(1), "x")
    val elem2_1 = childElem(pos(0), "y")
    val elem2_2 = childElem(pos(1), "y")
    val attributes = Seq(
      -\/(IndexableAttribute("xx", Seq(IndexableValue("1.1", -\/(elem1_1)), IndexableValue("2.1", -\/(elem1_2))), LiteralTypeConfig.Float)),
      -\/(IndexableAttribute("yy", Seq(IndexableValue("1.2", -\/(elem2_1)), IndexableValue("2.2", -\/(elem2_2))), LiteralTypeConfig.Float))
    )
    val indexableRootObject = IndexableRootObject("/db/a1", 7, 42, None, Some("1"), None, attributes)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"7/42/1","collection":"/db/a1","documentID":42,"xx":[123.4,456.12],"yy":[-17.45,15.67]}"""
  }

  def e18 = {
    val attr1 = attr(dom("""<w value="hello"/>"""), "value")
    val objects = Seq(\/-(IndexableObject("obj1", Seq(IndexableValue("1.1", \/-(attr1))), Map.empty)))
    val indexableRootObject = IndexableRootObject("/db/a1", 45, 48, None, Some("1"), None, objects)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"45/48/1","collection":"/db/a1","documentID":48,"obj1":"hello"}"""
  }

  def e19 = {
    val dom1 = dom("""<x><w value="hello"/><w value="world"/></x>""")
    val xs = elems(dom1, "w")
    val attr1_1 = attr(xs(0), "value")
    val attr1_2 = attr(xs(1), "value")
    val objects = Seq(\/-(IndexableObject("obj1", Seq(
      IndexableValue("1.1.1", \/-(attr1_1)),
      IndexableValue("1.2.1", \/-(attr1_2))
    ), Map.empty)))
    val indexableRootObject = IndexableRootObject("/db/a1", 46, 49, None, Some("1"), None, objects)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"46/49/1","collection":"/db/a1","documentID":49,"obj1":["hello","world"]}"""
  }

  def e20 = {
    val elem1 = elem(dom("""<w><x>hello</x><y>world</y></w>"""), "w")
    val objects = Seq(\/-(IndexableObject("obj1", Seq(IndexableValue("1.1", -\/(elem1))), Map.empty)))
    val indexableRootObject = IndexableRootObject("/db/a1", 5, 48, None, Some("1"), None, objects)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"5/48/1","collection":"/db/a1","documentID":48,"obj1":{"nodeId":"1.1","x":"hello","y":"world"}}"""
  }

  def e21 = {
    val elem1 = elem(dom("""<w><x>hello</x><y><z>world</z><zz>again</zz></y></w>"""), "w")
    val objects = Seq(\/-(IndexableObject("obj1", Seq(IndexableValue("1.1", -\/(elem1))), Map.empty)))
    val indexableRootObject = IndexableRootObject("/db/a1", 2, 49, None, Some("1"), None, objects)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"2/49/1","collection":"/db/a1","documentID":49,"obj1":{"nodeId":"1.1","x":"hello","y":{"z":"world","zz":"again"}}}"""
  }

  def e22 = {
    val elem1 = elem(dom("""<w><x>hello</x><y>world</y><y>again</y></w>"""), "w")
    val objects = Seq(\/-(IndexableObject("obj1", Seq(IndexableValue("1.1", -\/(elem1))), Map.empty)))
    val indexableRootObject = IndexableRootObject("/db/a1", 3, 50, None, Some("1"), None, objects)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"3/50/1","collection":"/db/a1","documentID":50,"obj1":{"nodeId":"1.1","x":"hello","y":["world","again"]}}"""
  }

  def e23 = {
    val elem1 = elem(dom("""<w><x>hello</x><y><yy>world</yy><yy>again</yy></y></w>"""), "w")
    val objects = Seq(\/-(IndexableObject("obj1", Seq(IndexableValue("1.1", -\/(elem1))), Map.empty)))
    val indexableRootObject = IndexableRootObject("/db/a1", 6, 51, None, Some("1"), None, objects)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"6/51/1","collection":"/db/a1","documentID":51,"obj1":{"nodeId":"1.1","x":"hello","y":{"yy":["world","again"]}}}"""
  }

  def e24 = {
    val dom1 = dom("""<parts><w><x>hello</x><y><yy>world</yy><yy>again</yy></y></w><w><x>goodbye</x><y><yy>until</yy><yy>next time</yy></y></w></parts>""")
    val ww = elems(dom1, "w")
    val objects = Seq(\/-(IndexableObject("obj1", Seq(
      IndexableValue("1.1", -\/(ww(0))),
      IndexableValue("1.2", -\/(ww(1)))
    ), Map.empty)))
    val indexableRootObject = IndexableRootObject("/db/a1", 6, 52, None, Some("1"), None, objects)
    serializeJson(indexableRootObject) mustEqual """{"objectID":"6/52/1","collection":"/db/a1","documentID":52,"obj1":[{"nodeId":"1.1","x":"hello","y":{"yy":["world","again"]}},{"nodeId":"1.2","x":"goodbye","y":{"yy":["until","next time"]}}]}"""
  }

  private def serializeJson(indexableRootObject: IndexableRootObject): String = {
    managed(new StringWriter).map { writer =>
      val mapper = new ObjectMapper
      mapper.writeValue(writer, indexableRootObject)
      writer.toString
    }.tried match {
      case Success(s) =>
        s
      case Failure(t) =>
        throw t
    }
  }

  private lazy val documentBuilderFactory = DocumentBuilderFactory.newInstance()
  private def dom(xml: String) : Document = {
    val documentBuilder = documentBuilderFactory.newDocumentBuilder()
    managed(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).map { is =>
      documentBuilder.parse(is)
    }.tried match {
      case Success(s) =>
        s
      case Failure(t) =>
        throw t
    }
  }

  private def attr(node: Node, name: String) : Attr = {
    if(node.isInstanceOf[Element]) {
      node.asInstanceOf[Element].getAttributeNode(name)
    } else if(node.isInstanceOf[Attr]) {
      node.asInstanceOf[Attr]
    } else if(node.isInstanceOf[Document]) {
      attr(node.asInstanceOf[Document].getDocumentElement, name)
    } else {
      throw new IllegalArgumentException
    }
  }

  private def elem(node: Node, name: String) : Element = {
    if(node.isInstanceOf[Element]) {
      val e = node.asInstanceOf[Element]
      if(Option(e.getLocalName).getOrElse(e.getNodeName) == name) {
        return e
      } else {
        val next = e.getNextSibling
        if(next != null) {
          return elem(next, name)
        }
      }
    } else if(node.isInstanceOf[Document]) {
      return elem(node.asInstanceOf[Document].getDocumentElement, name)
    }

    throw new IllegalArgumentException
  }

  private def childElem(elem: Element, name: String) : Element = {
    val childNodes = elem.getChildNodes
    (0 until childNodes.getLength)
      .map(childNodes.item(_))
      .filter(_.getNodeType == Node.ELEMENT_NODE)
      .map(_.asInstanceOf[Element])
      .filter(child => Option(child.getLocalName).getOrElse(child.getNodeName) == name)
      .headOption.getOrElse(null)
  }

  private def elems(node: Node, name: String) : Seq[Element] = {
    if(node.isInstanceOf[Element]) {
      val e = node.asInstanceOf[Element]
      if(Option(e.getLocalName).getOrElse(e.getNodeName) == name) {
        Option(e.getNextSibling) match {
          case Some(sibling) if(Option(e.getLocalName).getOrElse(e.getNodeName) == name) =>
            return Seq(e) ++ elems(sibling, name)
          case None =>
            return Seq(e)
        }
      } else {
        val next = Option(e.getNextSibling).getOrElse(e.getFirstChild)
        if(next != null) {
          return elems(next, name)
        }
      }
    } else if(node.isInstanceOf[Document]) {
      return elems(node.asInstanceOf[Document].getDocumentElement, name)
    }

    throw new IllegalArgumentException
  }

}