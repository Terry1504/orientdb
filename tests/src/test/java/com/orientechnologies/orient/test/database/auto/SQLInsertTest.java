/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.test.database.auto;

import com.orientechnologies.orient.core.command.script.OCommandScript;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.OClusterPosition;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.iterator.ORecordIteratorCluster;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.storage.OStorage;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * If some of the tests start to fail then check cluster number in queries, e.g #7:1. It can be because the order of clusters could
 * be affected due to adding or removing cluster from storage.
 */
@Test(groups = "sql-insert")
public class SQLInsertTest extends DocumentDBBaseTest {

  @Parameters(value = "url")
  public SQLInsertTest(@Optional String url) {
    super(url);
  }

  @Test
  public void insertOperator() {
    final int clId = database.addCluster("anotherdefault");
    final OClass profileClass = database.getMetadata().getSchema().getClass("Account");
    profileClass.addClusterId(clId);

    int addressId = database.getMetadata().getSchema().getClass("Address").getDefaultClusterId();

    List<OClusterPosition> positions = getValidPositions(addressId);

    ODocument doc = (ODocument) database.command(
        new OCommandSQL("insert into Profile (name, surname, salary, location, dummy) values ('Luca','Smith', 109.9, #" + addressId
            + ":" + positions.get(3) + ", 'hooray')")).execute();

    Assert.assertTrue(doc != null);
    Assert.assertEquals(doc.field("name"), "Luca");
    Assert.assertEquals(doc.field("surname"), "Smith");
    Assert.assertEquals(((Number) doc.field("salary")).floatValue(), 109.9f);
    Assert.assertEquals(doc.field("location", OType.LINK), new ORecordId(addressId, positions.get(3)));
    Assert.assertEquals(doc.field("dummy"), "hooray");

    doc = (ODocument) database.command(
        new OCommandSQL("insert into Profile SET name = 'Luca', surname = 'Smith', salary = 109.9, location = #" + addressId + ":"
            + positions.get(3) + ", dummy =  'hooray'")).execute();

    database.delete(doc);

    Assert.assertTrue(doc != null);
    Assert.assertEquals(doc.field("name"), "Luca");
    Assert.assertEquals(doc.field("surname"), "Smith");
    Assert.assertEquals(((Number) doc.field("salary")).floatValue(), 109.9f);
    Assert.assertEquals(doc.field("location", OType.LINK), new ORecordId(addressId, positions.get(3)));
    Assert.assertEquals(doc.field("dummy"), "hooray");
  }

  @Test
  public void insertWithWildcards() {
    int addressId = database.getMetadata().getSchema().getClass("Address").getDefaultClusterId();

    List<OClusterPosition> positions = getValidPositions(addressId);

    ODocument doc = (ODocument) database.command(
        new OCommandSQL("insert into Profile (name, surname, salary, location, dummy) values (?,?,?,?,?)")).execute("Marc",
        "Smith", 120.0, new ORecordId(addressId, positions.get(3)), "hooray");

    Assert.assertTrue(doc != null);
    Assert.assertEquals(doc.field("name"), "Marc");
    Assert.assertEquals(doc.field("surname"), "Smith");
    Assert.assertEquals(((Number) doc.field("salary")).floatValue(), 120.0f);
    Assert.assertEquals(doc.field("location", OType.LINK), new ORecordId(addressId, positions.get(3)));
    Assert.assertEquals(doc.field("dummy"), "hooray");

    database.delete(doc);

    doc = (ODocument) database.command(
        new OCommandSQL("insert into Profile SET name = ?, surname = ?, salary = ?, location = ?, dummy = ?")).execute("Marc",
        "Smith", 120.0, new ORecordId(addressId, positions.get(3)), "hooray");

    Assert.assertTrue(doc != null);
    Assert.assertEquals(doc.field("name"), "Marc");
    Assert.assertEquals(doc.field("surname"), "Smith");
    Assert.assertEquals(((Number) doc.field("salary")).floatValue(), 120.0f);
    Assert.assertEquals(doc.field("location", OType.LINK), new ORecordId(addressId, positions.get(3)));
    Assert.assertEquals(doc.field("dummy"), "hooray");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void insertMap() {
    ODocument doc = (ODocument) database
        .command(
            new OCommandSQL(
                "insert into cluster:default (equaledges, name, properties) values ('no', 'circle', {'round':'eeee', 'blaaa':'zigzag'} )"))
        .execute();

    Assert.assertTrue(doc != null);

    doc = (ODocument) new ODocument(doc.getIdentity()).load();

    Assert.assertEquals(doc.field("equaledges"), "no");
    Assert.assertEquals(doc.field("name"), "circle");
    Assert.assertTrue(doc.field("properties") instanceof Map);

    Map<Object, Object> entries = ((Map<Object, Object>) doc.field("properties"));
    Assert.assertEquals(entries.size(), 2);

    Assert.assertEquals(entries.get("round"), "eeee");
    Assert.assertEquals(entries.get("blaaa"), "zigzag");

    database.delete(doc);

    doc = (ODocument) database
        .command(
            new OCommandSQL(
                "insert into cluster:default SET equaledges = 'no', name = 'circle', properties = {'round':'eeee', 'blaaa':'zigzag'} "))
        .execute();

    Assert.assertTrue(doc != null);

    doc = (ODocument) new ODocument(doc.getIdentity()).load();

    Assert.assertEquals(doc.field("equaledges"), "no");
    Assert.assertEquals(doc.field("name"), "circle");
    Assert.assertTrue(doc.field("properties") instanceof Map);

    entries = ((Map<Object, Object>) doc.field("properties"));
    Assert.assertEquals(entries.size(), 2);

    Assert.assertEquals(entries.get("round"), "eeee");
    Assert.assertEquals(entries.get("blaaa"), "zigzag");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void insertList() {
    ODocument doc = (ODocument) database.command(
        new OCommandSQL(
            "insert into cluster:default (equaledges, name, list) values ('yes', 'square', ['bottom', 'top','left','right'] )"))
        .execute();

    Assert.assertTrue(doc != null);

    doc = (ODocument) new ODocument(doc.getIdentity()).load();

    Assert.assertEquals(doc.field("equaledges"), "yes");
    Assert.assertEquals(doc.field("name"), "square");
    Assert.assertTrue(doc.field("list") instanceof List);

    List<Object> entries = ((List<Object>) doc.field("list"));
    Assert.assertEquals(entries.size(), 4);

    Assert.assertEquals(entries.get(0), "bottom");
    Assert.assertEquals(entries.get(1), "top");
    Assert.assertEquals(entries.get(2), "left");
    Assert.assertEquals(entries.get(3), "right");

    database.delete(doc);

    doc = (ODocument) database.command(
        new OCommandSQL(
            "insert into cluster:default SET equaledges = 'yes', name = 'square', list = ['bottom', 'top','left','right'] "))
        .execute();

    Assert.assertTrue(doc != null);

    doc = (ODocument) new ODocument(doc.getIdentity()).load();

    Assert.assertEquals(doc.field("equaledges"), "yes");
    Assert.assertEquals(doc.field("name"), "square");
    Assert.assertTrue(doc.field("list") instanceof List);

    entries = ((List<Object>) doc.field("list"));
    Assert.assertEquals(entries.size(), 4);

    Assert.assertEquals(entries.get(0), "bottom");
    Assert.assertEquals(entries.get(1), "top");
    Assert.assertEquals(entries.get(2), "left");
    Assert.assertEquals(entries.get(3), "right");
  }

  @Test
  public void insertWithNoSpaces() {
    ODocument doc = (ODocument) database.command(
        new OCommandSQL("insert into cluster:default(id, title)values(10, 'NoSQL movement')")).execute();

    Assert.assertTrue(doc != null);
  }

  @Test
  public void insertAvoidingSubQuery() {
    final OSchema schema = database.getMetadata().getSchema();
    if (schema.getClass("test") == null)
      schema.createClass("test");

    ODocument doc = (ODocument) database.command(new OCommandSQL("INSERT INTO test(text) VALUES ('(Hello World)')")).execute();

    Assert.assertTrue(doc != null);
    Assert.assertEquals(doc.field("text"), "(Hello World)");
  }

  @Test
  public void insertSubQuery() {
    final OSchema schema = database.getMetadata().getSchema();
    if (schema.getClass("test") == null)
      schema.createClass("test");

    ODocument doc = (ODocument) database.command(new OCommandSQL("INSERT INTO test SET names = (select name from OUser)"))
        .execute();

    Assert.assertTrue(doc != null);
    Assert.assertNotNull(doc.field("names"));
    Assert.assertTrue(doc.field("names") instanceof Collection);
    Assert.assertEquals(((Collection<?>) doc.field("names")).size(), 3);
  }

  @Test
  public void insertCluster() {
    ODocument doc = database.command(
        new OCommandSQL("insert into Account cluster anotherdefault (id, title) values (10, 'NoSQL movement')")).execute();

    Assert.assertTrue(doc != null);
    Assert.assertEquals(doc.getIdentity().getClusterId(), database.getClusterIdByName("anotherdefault"));
    Assert.assertEquals(doc.getClassName(), "Account");
  }

  public void updateMultipleFields() {
    List<OClusterPosition> positions = getValidPositions(3);

    OIdentifiable result = database.command(
        new OCommandSQL("  INSERT INTO Account SET id= 3232,name= 'my name',map= {\"key\":\"value\"},dir= '',user= #3:"
            + positions.get(0))).execute();
    Assert.assertNotNull(result);

    ODocument record = result.getRecord();

    Assert.assertEquals(record.field("id"), 3232);
    Assert.assertEquals(record.field("name"), "my name");
    Map<String, String> map = record.field("map");
    Assert.assertTrue(map.get("key").equals("value"));
    Assert.assertEquals(record.field("dir"), "");
    Assert.assertEquals(record.field("user", OType.LINK), new ORecordId(3, positions.get(0)));
  }

  public void insertSelect() {
    database.command(new OCommandSQL("CREATE CLASS UserCopy")).execute();
    database.getMetadata().getSchema().reload();

    long inserted = database.command(new OCommandSQL("INSERT INTO UserCopy FROM select from ouser where name <> 'admin' limit 2"))
        .execute();
    Assert.assertEquals(inserted, 2);

    List<OIdentifiable> result = database.query(new OSQLSynchQuery<OIdentifiable>("select from UserCopy"));
    Assert.assertEquals(result.size(), 2);
    for (OIdentifiable r : result) {
      Assert.assertEquals(((ODocument) r.getRecord()).getClassName(), "UserCopy");
      Assert.assertNotSame(((ODocument) r.getRecord()).field("name"), "admin");
    }
  }

  private List<OClusterPosition> getValidPositions(int clusterId) {
    final List<OClusterPosition> positions = new ArrayList<OClusterPosition>();

    final ORecordIteratorCluster<?> iteratorCluster = database.browseCluster(database.getClusterNameById(clusterId));

    for (int i = 0; i < 100; i++) {
      if (!iteratorCluster.hasNext())
        break;
      ORecord doc = iteratorCluster.next();
      positions.add(doc.getIdentity().getClusterPosition());
    }
    return positions;
  }

  public void insertWithReturn() {

    if (!database.getMetadata().getSchema().existsClass("actor2")) {
      database.command(new OCommandSQL("CREATE CLASS Actor2")).execute();
      database.getMetadata().getSchema().reload();
    }

    // RETURN with $current.
    ODocument doc = database.command(new OCommandSQL("INSERT INTO Actor2 SET FirstName=\"FFFF\" RETURN $current")).execute();
    Assert.assertTrue(doc != null);
    Assert.assertEquals(doc.getClassName(), "Actor2");

    // RETURN with @rid
    Object res1 = database.command(new OCommandSQL("INSERT INTO Actor2 SET FirstName=\"Butch 1\" RETURN @rid")).execute();
    Assert.assertTrue(res1 instanceof ORecordId);
    Assert.assertTrue(((OIdentifiable) res1).getIdentity().isValid());

    // Create many records and return @rid
    Object res2 = database.command(
        new OCommandSQL(
            "INSERT INTO Actor2(FirstName,LastName) VALUES ('Jay','Miner'),('Frank','Hermier'),('Emily','Saut')  RETURN @rid"))
        .execute();
    Assert.assertTrue(res2 instanceof List<?>);
    Assert.assertTrue(((List) res2).get(0) instanceof ORecordId);

    // Create many records by INSERT INTO ...FROM and return wrapped field
    ORID another = ((OIdentifiable) res1).getIdentity();
    final String sql = "INSERT INTO Actor2 RETURN $current.FirstName  FROM SELECT * FROM [" + doc.getIdentity().toString() + ","
        + another.toString() + "]";
    ArrayList res3 = database.command(new OCommandSQL(sql)).execute();
    Assert.assertEquals(res3.size(), 2);
    Assert.assertTrue(((List) res3).get(0) instanceof ODocument);
    final ODocument res3doc = (ODocument) res3.get(0);
    Assert.assertTrue(res3doc.containsField("result"));
    Assert.assertTrue("FFFF".equalsIgnoreCase((String) res3doc.field("result"))
        || "Butch 1".equalsIgnoreCase((String) res3doc.field("result")));
    Assert.assertTrue(res3doc.containsField("rid"));
    Assert.assertTrue(res3doc.containsField("version"));

    // create record using content keyword and update it in sql batch passing recordID between commands
    final String sql2 = "let var1=INSERT INTO Actor2 CONTENT {Name:\"content\"} RETURN $current.@rid\n"
        + "let var2=UPDATE $var1 SET Bingo=1 RETURN AFTER @rid\n" + "return $var2\n" + "end";
    List<?> res_sql2 = database.command(new OCommandScript("sql", sql2)).execute();
    Assert.assertEquals(res_sql2.size(), 1);
    Assert.assertTrue(((List) res_sql2).get(0) instanceof ORecordId);

    // create record using content keyword and update it in sql batch passing recordID between commands
    final String sql3 = "let var1=INSERT INTO Actor2 CONTENT {Name:\"Bingo owner\"} RETURN @this\n"
        + "let var2=UPDATE $var1 SET Bingo=1 RETURN AFTER\n" + "return $var2\n" + "end";
    List<?> res_sql3 = database.command(new OCommandScript("sql", sql3)).execute();
    Assert.assertEquals(res_sql3.size(), 1);
    Assert.assertTrue(((List) res_sql3).get(0) instanceof ODocument);
    final ODocument sql3doc = (ODocument) (((List) res_sql3).get(0));
    Assert.assertEquals(sql3doc.field("Bingo"), 1);
    Assert.assertEquals(sql3doc.field("Name"), "Bingo owner");
  }

}
