/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hpsf.basic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.poi.hpsf.HPSFRuntimeException;
import org.apache.poi.hpsf.IllegalPropertySetDataException;
import org.apache.poi.hpsf.MutableProperty;
import org.apache.poi.hpsf.MutablePropertySet;
import org.apache.poi.hpsf.MutableSection;
import org.apache.poi.hpsf.NoFormatIDException;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.UnsupportedVariantTypeException;
import org.apache.poi.hpsf.Variant;
import org.apache.poi.hpsf.VariantSupport;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LittleEndian;



/**
 * <p>Tests HPSF's writing functionality.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @since 2003-02-07
 * @version $Id$
 */
public class TestWrite extends TestCase
{

    static final String POI_FS = "TestHPSFWritingFunctionality.doc";

    static final int BYTE_ORDER = 0xfffe;
    static final int FORMAT     = 0x0000;
    static final int OS_VERSION = 0x00020A04;
    static final int[] SECTION_COUNT = {1, 2};
    static final boolean[] IS_SUMMARY_INFORMATION = {true, false};
    static final boolean[] IS_DOCUMENT_SUMMARY_INFORMATION = {false, true};

    POIFile[] poiFiles;



    /**
     * <p>Constructor</p>
     * 
     * @param name the test case's name
     */
    public TestWrite(final String name)
    {
        super(name);
    }



    /**
     * @see TestCase#setUp()
     */
    public void setUp()
    {
        VariantSupport.setLogUnsupportedTypes(false);
    }



    /**
     * <p>Writes an empty property set to a POIFS and reads it back
     * in.</p>
     * 
     * @exception IOException if an I/O exception occurs
     * @exception UnsupportedVariantTypeException if HPSF does not yet support
     * a variant type to be written
     */
    public void testNoFormatID()
        throws IOException, UnsupportedVariantTypeException
    {
        final File dataDir =
            new File(System.getProperty("HPSF.testdata.path"));
        final File filename = new File(dataDir, POI_FS);
        filename.deleteOnExit();

        /* Create a mutable property set with a section that does not have the
         * formatID set: */
        final OutputStream out = new FileOutputStream(filename);
        final POIFSFileSystem poiFs = new POIFSFileSystem();
        final MutablePropertySet ps = new MutablePropertySet();
        ps.clearSections();
        ps.addSection(new MutableSection());

        /* Write it to a POIFS and the latter to disk: */
        try
        {
            final ByteArrayOutputStream psStream = new ByteArrayOutputStream();
            ps.write(psStream);
            psStream.close();
            final byte[] streamData = psStream.toByteArray();
            poiFs.createDocument(new ByteArrayInputStream(streamData),
                                 SummaryInformation.DEFAULT_STREAM_NAME);
            poiFs.writeFilesystem(out);
            out.close();
            Assert.fail("Should have thrown a NoFormatIDException.");
        }
        catch (Exception ex)
        {
            Assert.assertTrue(ex instanceof NoFormatIDException);
        }
        finally
        {
            out.close();
        }
    }



    /**
     * <p>Writes an empty property set to a POIFS and reads it back
     * in.</p>
     * 
     * @exception IOException if an I/O exception occurs
     * @exception UnsupportedVariantTypeException if HPSF does not yet support
     * a variant type to be written
     */
    public void testWriteEmptyPropertySet()
        throws IOException, UnsupportedVariantTypeException
    {
        final File dataDir =
            new File(System.getProperty("HPSF.testdata.path"));
        final File filename = new File(dataDir, POI_FS);
        filename.deleteOnExit();

        /* Create a mutable property set and write it to a POIFS: */
        final OutputStream out = new FileOutputStream(filename);
        final POIFSFileSystem poiFs = new POIFSFileSystem();
        final MutablePropertySet ps = new MutablePropertySet();
        final MutableSection s = (MutableSection) ps.getSections().get(0);
        s.setFormatID(SectionIDMap.SUMMARY_INFORMATION_ID);

        final ByteArrayOutputStream psStream = new ByteArrayOutputStream();
        ps.write(psStream);
        psStream.close();
        final byte[] streamData = psStream.toByteArray();
        poiFs.createDocument(new ByteArrayInputStream(streamData),
                             SummaryInformation.DEFAULT_STREAM_NAME);
        poiFs.writeFilesystem(out);
        out.close();

        /* Read the POIFS: */
        final POIFSReader r = new POIFSReader();
        r.registerListener(new MyPOIFSReaderListener(),
                           SummaryInformation.DEFAULT_STREAM_NAME);
        r.read(new FileInputStream(filename));
    }



    /**
     * <p>Writes a simple property set with a SummaryInformation section to a
     * POIFS and reads it back in.</p>
     * 
     * @exception IOException if an I/O exception occurs
     * @exception UnsupportedVariantTypeException if HPSF does not yet support
     * a variant type to be written
     */
    public void testWriteSimplePropertySet()
        throws IOException, UnsupportedVariantTypeException
    {
        final String AUTHOR = "Rainer Klute";
        final String TITLE = "Test Document"; 
        final File dataDir =
            new File(System.getProperty("HPSF.testdata.path"));
        final File filename = new File(dataDir, POI_FS);
        filename.deleteOnExit();
        final OutputStream out = new FileOutputStream(filename);
        final POIFSFileSystem poiFs = new POIFSFileSystem();
    
        final MutablePropertySet ps = new MutablePropertySet();
        final MutableSection si = new MutableSection();
        si.setFormatID(SectionIDMap.SUMMARY_INFORMATION_ID);
        ps.getSections().set(0, si);
    
        final MutableProperty p = new MutableProperty();
        p.setID(PropertyIDMap.PID_AUTHOR);
        p.setType(Variant.VT_LPWSTR);
        p.setValue(AUTHOR);
        si.setProperty(p);
        si.setProperty(PropertyIDMap.PID_TITLE, Variant.VT_LPSTR, TITLE);
    
        poiFs.createDocument(ps.toInputStream(),
                             SummaryInformation.DEFAULT_STREAM_NAME);
        poiFs.writeFilesystem(out);
        out.close();
    
        /* Read the POIFS: */
        final PropertySet[] psa = new PropertySet[1];
        final POIFSReader r = new POIFSReader();
        r.registerListener(new POIFSReaderListener()
            {
                public void processPOIFSReaderEvent
                    (final POIFSReaderEvent event)
                {
                    try
                    {
                        psa[0] = PropertySetFactory.create(event.getStream());
                    }
                    catch (Exception ex)
                    {
                        fail(org.apache.poi.hpsf.Util.toString(ex));
                    }
                }
    
            },
            SummaryInformation.DEFAULT_STREAM_NAME);
        r.read(new FileInputStream(filename));
        Assert.assertNotNull(psa[0]);
        final Section s = (Section) (psa[0].getSections().get(0));
        Object p1 = s.getProperty(PropertyIDMap.PID_AUTHOR);
        Object p2 = s.getProperty(PropertyIDMap.PID_TITLE);
        Assert.assertEquals(AUTHOR, p1);
        Assert.assertEquals(TITLE, p2);
    }



    /**
     * <p>Writes a simple property set with two sections to a POIFS and reads it
     * back in.</p>
     * 
     * @exception IOException if an I/O exception occurs
     * @exception WritingNotSupportedException if HPSF does not yet support
     * a variant type to be written
     */
    public void testWriteTwoSections()
        throws WritingNotSupportedException, IOException
    {
        final String STREAM_NAME = "PropertySetStream";
        final String SECTION1 = "Section 1";
        final String SECTION2 = "Section 2";

        final File dataDir =
            new File(System.getProperty("HPSF.testdata.path"));
        final File filename = new File(dataDir, POI_FS);
        filename.deleteOnExit();
        final OutputStream out = new FileOutputStream(filename);

        final POIFSFileSystem poiFs = new POIFSFileSystem();
        final MutablePropertySet ps = new MutablePropertySet();
        ps.clearSections();

        final byte[] formatID =
            new byte[]{0, 1,  2,  3,  4,  5,  6,  7,
                       8, 9, 10, 11, 12, 13, 14, 15};
        final MutableSection s1 = new MutableSection();
        s1.setFormatID(formatID);
        s1.setProperty(2, SECTION1);
        ps.addSection(s1);

        final MutableSection s2 = new MutableSection();
        s2.setFormatID(formatID);
        s2.setProperty(2, SECTION2);
        ps.addSection(s2);

        poiFs.createDocument(ps.toInputStream(), STREAM_NAME);
        poiFs.writeFilesystem(out);
        out.close();

        /* Read the POIFS: */
        final PropertySet[] psa = new PropertySet[1];
        final POIFSReader r = new POIFSReader();
        r.registerListener(new POIFSReaderListener()
            {
                public void processPOIFSReaderEvent
                    (final POIFSReaderEvent event)
                {
                    try
                    {
                        psa[0] = PropertySetFactory.create(event.getStream());
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                        throw new RuntimeException(ex.toString());
                        /* FIXME (2): Replace the previous line by the following
                         * one once we no longer need JDK 1.3 compatibility. */
                        // throw new RuntimeException(ex);
                    }
                }
            },
            STREAM_NAME);
        r.read(new FileInputStream(filename));
        Assert.assertNotNull(psa[0]);
        Section s = (Section) (psa[0].getSections().get(0));
        Object p = s.getProperty(2);
        Assert.assertEquals(SECTION1, p);
        s = (Section) (psa[0].getSections().get(1));
        p = s.getProperty(2);
        Assert.assertEquals(SECTION2, p);
    }



    static class MyPOIFSReaderListener implements POIFSReaderListener
    {
        public void processPOIFSReaderEvent(final POIFSReaderEvent event)
        {
            try
            {
                PropertySetFactory.create(event.getStream());
            }
            catch (Exception ex)
            {
                fail(org.apache.poi.hpsf.Util.toString(ex));
            }
        }
    }



    /**
     * <p>Writes and reads back various variant types and checks whether the
     * stuff that has been read back equals the stuff that was written.</p>
     */
    public void testVariantTypes()
    {
        Throwable t = null;
        final int codepage = -1;
        try
        {
            check(Variant.VT_EMPTY, null, codepage);
            check(Variant.VT_BOOL, new Boolean(true), codepage);
            check(Variant.VT_BOOL, new Boolean(false), codepage);
            check(Variant.VT_CF, new byte[]{0}, codepage);
            check(Variant.VT_CF, new byte[]{0, 1}, codepage);
            check(Variant.VT_CF, new byte[]{0, 1, 2}, codepage);
            check(Variant.VT_CF, new byte[]{0, 1, 2, 3}, codepage);
            check(Variant.VT_CF, new byte[]{0, 1, 2, 3, 4}, codepage);
            check(Variant.VT_CF, new byte[]{0, 1, 2, 3, 4, 5}, codepage);
            check(Variant.VT_CF, new byte[]{0, 1, 2, 3, 4, 5, 6}, codepage);
            check(Variant.VT_CF, new byte[]{0, 1, 2, 3, 4, 5, 6, 7}, codepage);
            check(Variant.VT_I2, new Integer(27), codepage);
            check(Variant.VT_I4, new Long(28), codepage);
            check(Variant.VT_FILETIME, new Date(), codepage);
            check(Variant.VT_LPSTR, "", codepage);
            check(Variant.VT_LPSTR, "�", codepage);
            check(Variant.VT_LPSTR, "��", codepage);
            check(Variant.VT_LPSTR, "���", codepage);
            check(Variant.VT_LPSTR, "����", codepage);
            check(Variant.VT_LPSTR, "�����", codepage);
            check(Variant.VT_LPSTR, "������", codepage);
            check(Variant.VT_LPSTR, "�������", codepage);
            check(Variant.VT_LPWSTR, "", codepage);
            check(Variant.VT_LPWSTR, "�", codepage);
            check(Variant.VT_LPWSTR, "��", codepage);
            check(Variant.VT_LPWSTR, "���", codepage);
            check(Variant.VT_LPWSTR, "����", codepage);
            check(Variant.VT_LPWSTR, "�����", codepage);
            check(Variant.VT_LPWSTR, "������", codepage);
            check(Variant.VT_LPWSTR, "�������", codepage);
        }
        catch (Exception ex)
        {
            t = ex;
        }
        catch (Error ex)
        {
            t = ex;
        }
        if (t != null)
            fail(org.apache.poi.hpsf.Util.toString(t));
    }



    /**
     * <p>Writes and reads back strings using several different codepages and
     * checks whether the stuff that has been read back equals the stuff that
     * was written.</p>
     */
    public void testCodepages()
    {
        Throwable t = null;
        final int[] validCodepages = new int[] {-1, 1252, 1200, 65001};
        for (int i = 0; i < validCodepages.length; i++)
        {
            int codepage = validCodepages[i];
            try
            {
                check(Variant.VT_LPSTR, "", codepage);
                check(Variant.VT_LPSTR, "�", codepage);
                check(Variant.VT_LPSTR, "��", codepage);
                check(Variant.VT_LPSTR, "���", codepage);
                check(Variant.VT_LPSTR, "����", codepage);
                check(Variant.VT_LPSTR, "�����", codepage);
                check(Variant.VT_LPSTR, "������", codepage);
                check(Variant.VT_LPSTR, "�������", codepage);
                check(Variant.VT_LPSTR, "\u79D1\u5B78", codepage);
            }
            catch (Exception ex)
            {
                t = ex;
            }
            catch (Error ex)
            {
                t = ex;
            }
            if (t != null)
                fail(org.apache.poi.hpsf.Util.toString(t));
        }

        final int[] invalidCodepages = new int[] {0, 1, 2, 4711, 815};
        for (int i = 0; i < invalidCodepages.length; i++)
        {
            int codepage = invalidCodepages[i];
            try
            {
                check(Variant.VT_LPSTR, "", codepage);
                check(Variant.VT_LPSTR, "�", codepage);
                check(Variant.VT_LPSTR, "��", codepage);
                check(Variant.VT_LPSTR, "���", codepage);
                check(Variant.VT_LPSTR, "����", codepage);
                check(Variant.VT_LPSTR, "�����", codepage);
                check(Variant.VT_LPSTR, "������", codepage);
                check(Variant.VT_LPSTR, "�������", codepage);
                fail("UnsupportedEncodingException for codepage " + codepage +
                     " expected.");
            }
            catch (UnsupportedEncodingException ex)
            {
                /* This is the expected behaviour. */
            }
            catch (Exception ex)
            {
                t = ex;
            }
            catch (Error ex)
            {
                t = ex;
            }
            if (t != null)
                fail(org.apache.poi.hpsf.Util.toString(t));
        }

    }



    /**
     * <p>Writes a property and reads it back in.</p>
     *
     * @param variantType The property's variant type.
     * @param value The property's value.
     * @throws UnsupportedVariantTypeException if the variant is not supported.
     * @throws IOException if an I/O exception occurs.
     */
    private void check(final long variantType, final Object value, 
                       final int codepage)
        throws UnsupportedVariantTypeException, IOException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        VariantSupport.write(out, variantType, value, codepage);
        out.close();
        final byte[] b = out.toByteArray();
        final Object objRead =
            VariantSupport.read(b, 0, b.length + LittleEndian.INT_SIZE,
                                variantType, codepage);
        if (objRead instanceof byte[])
        {
            final int diff = diff((byte[]) value, (byte[]) objRead);
            if (diff >= 0)
                fail("Byte arrays are different. First different byte is at " +
                     "index " + diff + ".");
        }
        else
            assertEquals(value, objRead);
    }



    /**
     * <p>Compares two byte arrays.</p>
     *
     * @param a The first byte array
     * @param b The second byte array
     * @return The index of the first byte that is different. If the byte arrays
     * are equal, -1 is returned.
     */
    private int diff(final byte[] a, final byte[] b)
    {
        final int min = Math.min(a.length, b.length);
        for (int i = 0; i < min; i++)
            if (a[i] != b[i])
                return i;
        if (a.length != b.length)
            return min;
        return -1;
    }



    /**
     * <p>This test method does a write and read back test with all POI
     * filesystems in the "data" directory by performing the following
     * actions for each file:</p>
     * 
     * <ul>
     * 
     * <li><p>Read its property set streams.</p></li>
     * 
     * <li><p>Create a new POI filesystem containing the origin file's
     * property set streams.</p></li>
     * 
     * <li><p>Read the property set streams from the POI filesystem just
     * created.</p></li>
     * 
     * <li><p>Compare each property set stream with the corresponding one from
     * the origin file and check whether they are equal.</p></li>
     *
     * </ul>
     */
    public void testRecreate()
    {
        final File dataDir =
            new File(System.getProperty("HPSF.testdata.path"));
        final File[] fileList = dataDir.listFiles(new FileFilter()
            {
                public boolean accept(final File f)
                {
                    return f.isFile();
                }
            });
        for (int i = 0; i < fileList.length; i++)
            testRecreate(fileList[i]);
    }



    /**
     * <p>Performs the check described in {@link #testRecreate()} for a single
     * POI filesystem.</p>
     *
     * @param f the POI filesystem to check
     */
    private void testRecreate(final File f)
    {
        try
        {
            /* Read the POI filesystem's property set streams: */
            final POIFile[] psf1 = Util.readPropertySets(f);

            /* Create a new POI filesystem containing the origin file's
             * property set streams: */
            final File copy = File.createTempFile(f.getName(), "");
            copy.deleteOnExit();
            final OutputStream out = new FileOutputStream(copy);
            final POIFSFileSystem poiFs = new POIFSFileSystem();
            for (int i = 0; i < psf1.length; i++)
            {
                final InputStream in =
                    new ByteArrayInputStream(psf1[i].getBytes());
                final PropertySet psIn = PropertySetFactory.create(in);
                final MutablePropertySet psOut = new MutablePropertySet(psIn);
                final ByteArrayOutputStream psStream =
                    new ByteArrayOutputStream();
                psOut.write(psStream);
                psStream.close();
                final byte[] streamData = psStream.toByteArray();
                poiFs.createDocument(new ByteArrayInputStream(streamData),
                                     psf1[i].getName());
                poiFs.writeFilesystem(out);
            }
            out.close();


            /* Read the property set streams from the POI filesystem just
             * created. */
            final POIFile[] psf2 = Util.readPropertySets(copy);
            for (int i = 0; i < psf2.length; i++)
            {
                final byte[] bytes1 = psf1[i].getBytes();
                final byte[] bytes2 = psf2[i].getBytes();
                final InputStream in1 = new ByteArrayInputStream(bytes1);
                final InputStream in2 = new ByteArrayInputStream(bytes2);
                final PropertySet ps1 = PropertySetFactory.create(in1);
                final PropertySet ps2 = PropertySetFactory.create(in2);
            
                /* Compare the property set stream with the corresponding one
                 * from the origin file and check whether they are equal. */
                assertEquals(ps1, ps2);
            }
        }
        catch (Exception ex)
        {
            handle(ex);
        }
    }



    /**
     * <p>Tests writing and reading back a proper dictionary.</p>
     */
    public void testDictionary()
    {
        try
        {
            final File copy = File.createTempFile("Test-HPSF", "ole2");
            copy.deleteOnExit();

            /* Write: */
            final OutputStream out = new FileOutputStream(copy);
            final POIFSFileSystem poiFs = new POIFSFileSystem();
            final MutablePropertySet ps1 = new MutablePropertySet();
            final MutableSection s = (MutableSection) ps1.getSections().get(0);
            final Map m = new HashMap(3, 1.0f);
            m.put(new Long(1), "String 1");
            m.put(new Long(2), "String 2");
            m.put(new Long(3), "String 3");
            s.setDictionary(m);
            s.setFormatID(SectionIDMap.DOCUMENT_SUMMARY_INFORMATION_ID);
            int codepage = Property.CP_UNICODE;
            s.setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2,
                          new Integer(codepage));
            poiFs.createDocument(ps1.toInputStream(), "Test");
            poiFs.writeFilesystem(out);
            out.close();

            /* Read back: */
            final POIFile[] psf = Util.readPropertySets(copy);
            Assert.assertEquals(1, psf.length);
            final byte[] bytes = psf[0].getBytes();
            final InputStream in = new ByteArrayInputStream(bytes);
            final PropertySet ps2 = PropertySetFactory.create(in);

            /* Compare the property set stream with the corresponding one
             * from the origin file and check whether they are equal. */
            assertEquals(ps1, ps2);
        }
        catch (Exception ex)
        {
            handle(ex);
        }
    }



    /**
     * <p>Tests writing and reading back a proper dictionary with an invalid
     * codepage. (HPSF writes Unicode dictionaries only.)</p>
     */
    public void testDictionaryWithInvalidCodepage()
    {
        try
        {
            final File copy = File.createTempFile("Test-HPSF", "ole2");
            copy.deleteOnExit();

            /* Write: */
            final OutputStream out = new FileOutputStream(copy);
            final POIFSFileSystem poiFs = new POIFSFileSystem();
            final MutablePropertySet ps1 = new MutablePropertySet();
            final MutableSection s = (MutableSection) ps1.getSections().get(0);
            final Map m = new HashMap(3, 1.0f);
            m.put(new Long(1), "String 1");
            m.put(new Long(2), "String 2");
            m.put(new Long(3), "String 3");
            s.setDictionary(m);
            s.setFormatID(SectionIDMap.DOCUMENT_SUMMARY_INFORMATION_ID);
            int codepage = 12345;
            s.setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2,
                          new Integer(codepage));
            poiFs.createDocument(ps1.toInputStream(), "Test");
            poiFs.writeFilesystem(out);
            out.close();
            fail("This testcase did not detect the invalid codepage value.");
        }
        catch (IllegalPropertySetDataException ex)
        {
            assertTrue(true);
        }
        catch (Exception ex)
        {
            handle(ex);
        }
    }



    /**
     * <p>Handles unexpected exceptions in testcases.</p>
     *
     * @param ex The exception that has been thrown.
     */
    private void handle(final Exception ex)
    {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        Throwable t = ex;
        while (t != null)
        {
            t.printStackTrace(pw);
            if (t instanceof HPSFRuntimeException)
                t = ((HPSFRuntimeException) t).getReason();
            else
                t = null;
            if (t != null)
                pw.println("Caused by:");
        }
        pw.close();
        try
        {
            sw.close();
        }
        catch (IOException ex2)
        {
            ex.printStackTrace();
        }
        fail(sw.toString());
    }


    /**
     * <p>Runs the test cases stand-alone.</p>
     */
    public static void main(final String[] args) throws Throwable
    {
        System.setProperty("HPSF.testdata.path",
                           "./src/testcases/org/apache/poi/hpsf/data");
        junit.textui.TestRunner.run(TestWrite.class);
    }

}
