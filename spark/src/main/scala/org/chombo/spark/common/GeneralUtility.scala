/*
 * chombo: on spark
 * Author: Pranab Ghosh
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.chombo.spark.common

import org.apache.spark.rdd.RDD
import scala.collection.JavaConverters._
import org.chombo.util.BasicUtils
import scala.collection.mutable.HashSet
import org.chombo.util.BasicUtils

trait GeneralUtility {

  /**
  * @param keyFields
  * @return
  */
  def getKeyFieldOrdinals(keyFields:Option[java.util.List[Integer]]) : Option[Array[Integer]] = {
    keyFields match {
	     case Some(fields:java.util.List[Integer]) => Some(fields.asScala.toArray)
	     case None => None  
	}
  }
  
  
  /**
  * @param list
  * @return
  */
  def toOptionalIntegerArray(list:Option[java.util.List[Integer]]) : Option[Array[Integer]] = {
    list match {
	     case Some(list:java.util.List[Integer]) => Some(list.asScala.toArray)
	     case None => None  
	}
  }

  /**
  * @param list
  * @return
  */
  def toOptionalIntArray(list:Option[java.util.List[Integer]]) : Option[Array[Int]] = {
    list match {
	     case Some(list:java.util.List[Integer]) => Some(list.asScala.toArray.map(i => i.toInt))
	     case None => None  
	}
  }

  /**
  * @param list
  * @return
  */
  def toIntArray(list:java.util.List[Integer]) : Array[Int] = list.asScala.toArray.map(i => i.toInt)

  /**
  * @param list
  * @return
  */
  def toIntegerArray(list:java.util.List[Integer]) : Array[Integer] = list.asScala.toArray
 
  /**
  * @param list
  * @return
  */
  def toOptionalIntList(list:Option[java.util.List[Integer]]) : Option[List[Int]] = {
    list match {
	     case Some(list:java.util.List[Integer]) => {
	       val sArray = list.asScala.toList.map(i => i.toInt)
	       Some(sArray)
	     }
	     case None => None  
	}
  }
  
  /**
  * @param list
  * @return
  */
  def toIntList(list:java.util.List[Integer]) : List[Int] = {
    list.asScala.toList.map(i => i.toInt)
  }

  /**
  * @param list
  * @return
  */
  def toIntegerList(list:java.util.List[Integer]) : List[Integer] = {
    list.asScala.toList
  }

  /**
  * @param arr
  * @return
  */
  def fromOptionalIntegerToIntArray(arr:Option[Array[Integer]]) : Option[Array[Int]] = {
    arr match {
	     case Some(a:Array[Integer]) => Some(a.map(i => i.toInt))
	     case None => None  
	}
  }
  
  /**
  * @param keyFieldOrdinals
  * @return
  */
  def getKeyLen(keyFieldOrdinals:Option[Array[Integer]]) : Int = {
	  keyFieldOrdinals match {
	    case Some(fields : Array[Integer]) => fields.length
	    case None => 0
	  }
  }
  
  /**
  * @param arr
  * @return
  */
  def getOptinalArrayLength[T](arr:Option[Array[T]], defLen:Int = 0) : Int = {
	  arr match {
	    case Some(arr : Array[T]) => arr.length
	    case None => defLen
	  }
  }
  

  /**
  * @param list
  * @param defLen
  * @return
  */
  def getOptinalListLength[T](list:Option[List[T]], defLen:Int = 0) : Int = {
	  list match {
	    case Some(list : List[T]) => list.length
	    case None => defLen
	  }
  }

  /**
  * @param fields
  * @param keyFieldOrdinals
  * @param rec
  */
  def populateFields(fields:Array[String], fieldOrdinals:Option[Array[Int]], rec:Record)  {
	  fieldOrdinals match {
	      case Some(fieldOrds : Array[Int]) => {
	    	  for (kf <- fieldOrds) {
	    		  rec.addString(fields(kf))
			  }
	      }
	      case None => 
	  }
   }
  
  /**
  * @param fields
  * @param keyFieldOrdinals
  * @param rec
  * @param defKey
  */
  def populateFields(fields:Array[String], fieldOrdinals:Option[Array[Int]], rec:Record, defKey:String)  {
	  fieldOrdinals match {
	      case Some(fieldOrds : Array[Int]) => {
	    	  for (kf <- fieldOrds) {
	    		  rec.addString(fields(kf))
			  }
	      }
	      case None => rec.add(defKey)
	  }
   }
  
  /**
  * @param recOne
  * @param recTwo
  * @return
  */
  def combineWithUniqueFields(recOne:Record, recTwo:Record) : Record = {
    val uniqueItems = HashSet[Any]()
    recOne.initialize()
    while(recOne.hasNext()) {
      uniqueItems.add(recOne.getAny)
    }
    recTwo.initialize()
    while(recTwo.hasNext()) {
      uniqueItems += recTwo.getAny
    }
    
    val newRec = Record(uniqueItems.size)
    uniqueItems.foreach(i => newRec.add(i))
    newRec
  }

  /**
 * @param data
 * @param fieldDelimIn
 * @param keyDefined
 * @param keyLen
 * @param keyFieldOrdinals
 * @param seqFieldOrd
 * @return
 */
  def replTimestampWithSeq(data:RDD[String], fieldDelimIn:String, keyDefined:Boolean, keyLen:Int, 
      keyFieldOrdinals:Option[Array[Integer]], seqFieldOrd:Int) : RDD[String] = {
	  if (keyDefined) {
		  data.map(line => {
			   val fields = BasicUtils.getTrimmedFields(line, fieldDelimIn)
			   val key = Record(keyLen)
	           Record.populateFields(fields, keyFieldOrdinals, key)
	           val seq = fields(seqFieldOrd).toLong
	           key.addLong(seq)
	           (key, line)
		  }).sortByKey(true).zipWithIndex.map(z => {
		     val line = z._1._2
		     val fields = BasicUtils.getTrimmedFields(line, fieldDelimIn)
		     fields(seqFieldOrd) = z._2.toString
		     fields.mkString(fieldDelimIn)
		  })
	  } else {
		  data.sortBy(line => {
		    val fields = BasicUtils.getTrimmedFields(line, fieldDelimIn)
		    fields(seqFieldOrd).toLong
		  }, true).zipWithIndex.map(z => {
		    val line = z._1
		    val fields = BasicUtils.getTrimmedFields(line, fieldDelimIn)
		    fields(seqFieldOrd) = z._2.toString
		    fields.mkString(fieldDelimIn)
		  })
	  }
  }
  
  /**
  * @param data
  * @return
  */
  def findAverageDouble(data:RDD[(Record, (Int, Double))]) : RDD[(Record, Double)] = {
    data.reduceByKey((v1, v2) => {
      val count = v1._1 + v2._1
      val sum = v1._2 + v2._2
      (count, sum)
	}).mapValues(v => v._2 / v._1)
  }
  
  def findAverageInt(data:RDD[(Record, (Int, Int))]) : RDD[(Record, Int)] = {
    data.reduceByKey((v1, v2) => {
      val count = v1._1 + v2._1
      val sum = v1._2 + v2._2
      (count, sum)
	}).mapValues(v => v._2 / v._1)
  }

  /**
  * @param data
  * @return
  */
  def findMinMaxDouble(data:RDD[(Record,(Double,Double))]) : RDD[(Record, (Double,Double))] = {
    data.reduceByKey((v1, v2) => {
	  val min = if (v1._1 < v2._1) v1._1 else v2._1
	  val max = if (v1._2 > v2._2) v1._2 else v2._2
	  (min, max)
      (min, max)
	})
  }
  
  /**
  * @param data
  * @return
  */
  def findMinMaxInt(data:RDD[(Record,(Int,Int))]) : RDD[(Record, (Int,Int))] = {
    data.reduceByKey((v1, v2) => {
	  val min = if (v1._1 < v2._1) v1._1 else v2._1
	  val max = if (v1._2 > v2._2) v1._2 else v2._2
	  (min, max)
      (min, max)
	})
  }
  
  /**
  * @param map
  * @param key
  * @param errMsg
  * @return
  */
  def getMapValue[K,V](map:scala.collection.Map[K,V], key:K, errMsg:String) : V = {
     val va = map.get(key) match {
       case Some(v) => v
       case None => BasicUtils.assertCondition(false, errMsg)
     }
     va.asInstanceOf[V]
  }
}