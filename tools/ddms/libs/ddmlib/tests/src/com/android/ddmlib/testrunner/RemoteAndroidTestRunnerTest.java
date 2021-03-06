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

package com.android.ddmlib.testrunner;

import com.android.ddmlib.Client;
import com.android.ddmlib.FileListingService;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.Device.DeviceState;
import com.android.ddmlib.log.LogReceiver;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Map;

/**
 * Test RemoteAndroidTestRunner. 
 */
public class RemoteAndroidTestRunnerTest extends TestCase {

    private RemoteAndroidTestRunner mRunner;
    private MockDevice mMockDevice;
    
    private static final String TEST_PACKAGE = "com.test";
    private static final String TEST_RUNNER = "com.test.InstrumentationTestRunner";
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        mMockDevice = new MockDevice();
        mRunner = new RemoteAndroidTestRunner(TEST_PACKAGE, TEST_RUNNER, mMockDevice);
    }
    
    /**
     * Test the basic case building of the instrumentation runner command with no arguments
     */
    public void testRun() {
        mRunner.run(new EmptyListener());
        assertStringsEquals(String.format("am instrument -w -r %s/%s", TEST_PACKAGE, TEST_RUNNER), 
                mMockDevice.getLastShellCommand());
    }

    /**
     * Test the building of the instrumentation runner command with log set
     */
    public void testRunWithLog() {
        mRunner.setLogOnly(true);
        mRunner.run(new EmptyListener());
        assertStringsEquals(String.format("am instrument -w -r -e log true %s/%s", TEST_PACKAGE, 
                TEST_RUNNER), mMockDevice.getLastShellCommand());
    }

    /**
     * Test the building of the instrumentation runner command with method set
     */
    public void testRunWithMethod() {
        final String className = "FooTest";
        final String testName = "fooTest";
        mRunner.setMethodName(className, testName);
        mRunner.run(new EmptyListener());
        assertStringsEquals(String.format("am instrument -w -r -e class %s#%s %s/%s", className, 
                testName, TEST_PACKAGE, TEST_RUNNER), mMockDevice.getLastShellCommand());
    }
    
    /**
     * Test the building of the instrumentation runner command with extra args set
     */
    public void testRunWithExtraArgs() {
        final String extraArgs = "blah";
        mRunner.setExtraArgs(extraArgs);
        mRunner.run(new EmptyListener());
        assertStringsEquals(String.format("am instrument -w -r %s %s/%s", extraArgs,
                TEST_PACKAGE, TEST_RUNNER), mMockDevice.getLastShellCommand());
    }


    /**
     * Assert two strings are equal ignoring whitespace
     */
    private void assertStringsEquals(String str1, String str2) {
        String strippedStr1 = str1.replaceAll(" ", "");
        String strippedStr2 = str2.replaceAll(" ", "");
        assertEquals(strippedStr1, strippedStr2);
    }
    
    /**
     * A dummy device that does nothing except store the provided executed shell command for 
     * later retrieval
     */
    private static class MockDevice implements IDevice {

        private String mLastShellCommand;
        
        /**
         * Stores the provided command for later retrieval from getLastShellCommand
         */
        public void executeShellCommand(String command,
                IShellOutputReceiver receiver) throws IOException {
            mLastShellCommand = command;
        }
        
        /**
         * Get the last command provided to executeShellCommand
         */
        public String getLastShellCommand() {
            return mLastShellCommand;
        }
        
        public boolean createForward(int localPort, int remotePort) {
            throw new UnsupportedOperationException();
        }

        public Client getClient(String applicationName) {
            throw new UnsupportedOperationException();
        }

        public String getClientName(int pid) {
            throw new UnsupportedOperationException();
        }

        public Client[] getClients() {
            throw new UnsupportedOperationException();
        }

        public FileListingService getFileListingService() {
            throw new UnsupportedOperationException();
        }

        public Map<String, String> getProperties() {
            throw new UnsupportedOperationException();
        }

        public String getProperty(String name) {
            throw new UnsupportedOperationException();
        }

        public int getPropertyCount() {
            throw new UnsupportedOperationException();
        }

        public RawImage getScreenshot() throws IOException {
            throw new UnsupportedOperationException();
        }

        public String getSerialNumber() {
            throw new UnsupportedOperationException();
        }

        public DeviceState getState() {
            throw new UnsupportedOperationException();
        }

        public SyncService getSyncService() {
            throw new UnsupportedOperationException();
        }

        public boolean hasClients() {
            throw new UnsupportedOperationException();
        }

        public boolean isBootLoader() {
            throw new UnsupportedOperationException();
        }

        public boolean isEmulator() {
            throw new UnsupportedOperationException();
        }

        public boolean isOffline() {
            throw new UnsupportedOperationException();
        }

        public boolean isOnline() {
            throw new UnsupportedOperationException();
        }

        public boolean removeForward(int localPort, int remotePort) {
            throw new UnsupportedOperationException();
        }

        public void runEventLogService(LogReceiver receiver) throws IOException {
            throw new UnsupportedOperationException();
        }

        public String getVmName() {
            return "";
        }

    }
    
    /** An empty implementation of TestRunListener
     */
    private static class EmptyListener implements ITestRunListener {

        public void testEnded(String className, String testName) {
            // ignore
        }

        public void testFailed(int status, String className, String testName,
                String trace) {
            // ignore
        }

        public void testRunEnded(long elapsedTime) {
            // ignore
        }

        public void testRunFailed(String errorMessage) {
            // ignore
        }

        public void testRunStarted(int testCount) {
            // ignore
        }

        public void testRunStopped(long elapsedTime) {
            // ignore
        }

        public void testStarted(String className, String testName) {
            // ignore
        }
        
    }
}
