/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdkmanager;

import junit.framework.TestCase;


public class CommandLineProcessorTest extends TestCase {

    /**
     * A mock version of the {@link CommandLineProcessor} class that does not
     * exits and captures its stdout/stderr output.
     */
    public static class MockCommandLineProcessor extends CommandLineProcessor {
        private boolean mExitCalled;
        private boolean mHelpCalled;
        private String mStdOut = "";
        private String mStdErr = "";
        
        public MockCommandLineProcessor() {
            super(new String[][] {
                    { "action1", "Some action" },
                    { "action2", "Another action" },
            });
            define(MODE.STRING, false /*mandatory*/,
                    "action1", "1", "first", "non-mandatory flag", null);
            define(MODE.STRING, true /*mandatory*/,
                    "action1", "2", "second", "mandatory flag", null);
        }
        
        @Override
        public void printHelpAndExitForAction(String actionFilter,
                String errorFormat, Object... args) {
            mHelpCalled = true;
            super.printHelpAndExitForAction(actionFilter, errorFormat, args);
        }
        
        @Override
        protected void exit() {
            mExitCalled = true;
        }
        
        @Override
        protected void stdout(String format, Object... args) {
            String s = String.format(format, args);
            mStdOut += s + "\n";
            // don't call super to avoid printing stuff
        }
        
        @Override
        protected void stderr(String format, Object... args) {
            String s = String.format(format, args);
            mStdErr += s + "\n";
            // don't call super to avoid printing stuff
        }
        
        public boolean wasHelpCalled() {
            return mHelpCalled;
        }
        
        public boolean wasExitCalled() {
            return mExitCalled;
        }
        
        public String getStdOut() {
            return mStdOut;
        }
        
        public String getStdErr() {
            return mStdErr;
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public final void testPrintHelpAndExit() {
        MockCommandLineProcessor c = new MockCommandLineProcessor();        
        assertFalse(c.wasExitCalled());
        assertFalse(c.wasHelpCalled());
        assertTrue(c.getStdOut().equals(""));
        assertTrue(c.getStdErr().equals(""));
        c.printHelpAndExit(null);
        assertTrue(c.getStdOut().indexOf("-v") != -1);
        assertTrue(c.getStdOut().indexOf("--verbose") != -1);
        assertTrue(c.getStdErr().equals(""));
        assertTrue(c.wasExitCalled());

        c = new MockCommandLineProcessor();        
        assertFalse(c.wasExitCalled());
        assertTrue(c.getStdOut().equals(""));
        assertTrue(c.getStdErr().indexOf("Missing parameter") == -1);

        c.printHelpAndExit("Missing %s", "parameter");
        assertTrue(c.wasExitCalled());
        assertFalse(c.getStdOut().equals(""));
        assertTrue(c.getStdErr().indexOf("Missing parameter") != -1);
    }
    
    public final void testVerbose() {
        MockCommandLineProcessor c = new MockCommandLineProcessor();        

        assertFalse(c.isVerbose());
        c.parseArgs(new String[] { "-v" });
        assertTrue(c.isVerbose());
        assertTrue(c.wasExitCalled());
        assertTrue(c.wasHelpCalled());
        assertTrue(c.getStdErr().indexOf("Missing action name.") != -1);

        c = new MockCommandLineProcessor();        
        c.parseArgs(new String[] { "--verbose" });
        assertTrue(c.isVerbose());
        assertTrue(c.wasExitCalled());
        assertTrue(c.wasHelpCalled());
        assertTrue(c.getStdErr().indexOf("Missing action name.") != -1);
    }
    
    public final void testHelp() {
        MockCommandLineProcessor c = new MockCommandLineProcessor();        

        c.parseArgs(new String[] { "-h" });
        assertTrue(c.wasExitCalled());
        assertTrue(c.wasHelpCalled());
        assertTrue(c.getStdErr().indexOf("Missing action name.") == -1);

        c = new MockCommandLineProcessor();        
        c.parseArgs(new String[] { "--help" });
        assertTrue(c.wasExitCalled());
        assertTrue(c.wasHelpCalled());
        assertTrue(c.getStdErr().indexOf("Missing action name.") == -1);
    }

    public final void testMandatory() {
        MockCommandLineProcessor c = new MockCommandLineProcessor();        

        c.parseArgs(new String[] { "action1", "-1", "value1", "-2", "value2" });
        assertFalse(c.wasExitCalled());
        assertFalse(c.wasHelpCalled());
        assertEquals("", c.getStdErr());
        assertEquals("value1", c.getValue("action1", "first"));
        assertEquals("value2", c.getValue("action1", "second"));

        c = new MockCommandLineProcessor();        
        c.parseArgs(new String[] { "action1", "-2", "value2" });
        assertFalse(c.wasExitCalled());
        assertFalse(c.wasHelpCalled());
        assertEquals("", c.getStdErr());
        assertEquals(null, c.getValue("action1", "first"));
        assertEquals("value2", c.getValue("action1", "second"));

        c = new MockCommandLineProcessor();        
        c.parseArgs(new String[] { "action1" });
        assertTrue(c.wasExitCalled());
        assertTrue(c.wasHelpCalled());
        assertTrue(c.getStdErr().indexOf("must be defined") != -1);
        assertEquals(null, c.getValue("action1", "first"));
        assertEquals(null, c.getValue("action1", "second"));
    }
}