/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.xslf.usermodel;

import static org.junit.Assert.*;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.junit.Before;
import org.junit.Test;
import org.openxmlformats.schemas.presentationml.x2006.main.*;

public class TestXMLSlideShow {
   private static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
   private OPCPackage pack;

   @Before
   public void setUp() throws Exception {
      pack = OPCPackage.open(slTests.openResourceAsStream("sample.pptx"));
   }

   @Test
   public void testContainsMainContentType() throws Exception {
      boolean found = false;
      for(PackagePart part : pack.getParts()) {
         if(part.getContentType().equals(XSLFRelation.MAIN.getContentType())) {
            found = true;
         }
      }
      assertTrue(found);
   }

   @Test
   public void testOpen() throws Exception {
      XMLSlideShow xml;

      // With the finalised uri, should be fine
      xml = new XMLSlideShow(pack);
      // Check the core
      assertNotNull(xml.getCTPresentation());

      // Check it has some slides
      assertFalse(xml.getSlides().isEmpty());
      assertFalse(xml.getSlideMasters().isEmpty());
   }

   @Test
   @SuppressWarnings("deprecation")
   public void testSlideBasics() throws Exception {
      XMLSlideShow xml = new XMLSlideShow(pack);

      // Should have 1 master
      assertEquals(1, xml.getSlideMasters().size());

      // Should have two sheets
      assertEquals(2, xml.getSlides().size());

      // Check they're as expected
      CTSlideIdListEntry[] slides = xml.getCTPresentation().getSldIdLst().getSldIdArray();

      assertEquals(256, slides[0].getId());
      assertEquals(257, slides[1].getId());
      assertEquals("rId2", slides[0].getId2());
      assertEquals("rId3", slides[1].getId2());

      // Now get those objects
      assertNotNull(xml.getSlides().get(0));
      assertNotNull(xml.getSlides().get(1));

      // And check they have notes as expected
      assertNotNull(xml.getSlides().get(0).getNotes());
      assertNotNull(xml.getSlides().get(1).getNotes());

      // Next up look for the slide master
      CTSlideMasterIdListEntry[] masters = xml.getCTPresentation().getSldMasterIdLst().getSldMasterIdArray();

      assertEquals(2147483648l, masters[0].getId());
      assertEquals("rId1", masters[0].getId2());
      assertNotNull(xml.getSlideMasters().get(0));

      // Finally look for the notes master
      CTNotesMasterIdListEntry notesMaster =
         xml.getCTPresentation().getNotesMasterIdLst().getNotesMasterId();
      assertNotNull(notesMaster);

      assertNotNull(xml.getNotesMaster());
   }

   @Test
   public void testMetadataBasics() throws Exception {
      XMLSlideShow xml = new XMLSlideShow(pack);

      assertNotNull(xml.getProperties().getCoreProperties());
      assertNotNull(xml.getProperties().getExtendedProperties());

      assertEquals("Microsoft Office PowerPoint", xml.getProperties().getExtendedProperties().getUnderlyingProperties().getApplication());
      assertEquals(0, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getCharacters());
      assertEquals(0, xml.getProperties().getExtendedProperties().getUnderlyingProperties().getLines());

      assertEquals(null, xml.getProperties().getCoreProperties().getTitle());
      assertEquals(null, xml.getProperties().getCoreProperties().getUnderlyingProperties().getSubjectProperty().getValue());
   }

   @Test
   public void testComments() throws Exception {
      // Default sample file has none
      XMLSlideShow xml = new XMLSlideShow(pack);

      assertEquals(null, xml.getCommentAuthors());

      for (XSLFSlide slide : xml.getSlides()) {
         assertEquals(null, slide.getComments());
      }

      // Try another with comments
      OPCPackage packComments = OPCPackage.open(slTests.openResourceAsStream("45545_Comment.pptx"));
      XMLSlideShow xmlComments = new XMLSlideShow(packComments);

      // Has one author
      assertNotNull(xmlComments.getCommentAuthors());
      assertEquals(1, xmlComments.getCommentAuthors().getCTCommentAuthorsList().sizeOfCmAuthorArray());
      assertEquals("XPVMWARE01", xmlComments.getCommentAuthors().getAuthorById(0).getName());

      // First two slides have comments
      int i = -1;
      for (XSLFSlide slide : xmlComments.getSlides()) {
         i++;

         if(i == 0) {
            assertNotNull(slide.getComments());
            assertEquals(1, slide.getComments().getNumberOfComments());
            assertEquals("testdoc", slide.getComments().getCommentAt(0).getText());
            assertEquals(0, slide.getComments().getCommentAt(0).getAuthorId());
         } else if (i == 1) {
            assertNotNull(slide.getComments());
            assertEquals(1, slide.getComments().getNumberOfComments());
            assertEquals("test phrase", slide.getComments().getCommentAt(0).getText());
            assertEquals(0, slide.getComments().getCommentAt(0).getAuthorId());
         } else {
            assertEquals(null, slide.getComments());
         }
      }
   }
}