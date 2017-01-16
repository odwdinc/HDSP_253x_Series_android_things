/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.androidthings.myproject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOError;
import java.io.IOException;
import static android.os.SystemClock.sleep;

/**
 * Skeleton of the main Android Things activity. Implement your device's logic
 * in this class.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 *
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();






    private Gpio D0;    //IO2
    private Gpio D1;    //IO3
    private Gpio D2;    //IO4
    private Gpio D3;    //IO5
    private Gpio D4;    //IO6
    private Gpio D5;    //IO7
    private Gpio D6;    //IO8
    private Gpio D7;    //IO9

    private Gpio A0;    //IO10
    private Gpio A1;    //IO11
    private Gpio A2;    //IO12
    private Gpio A3;    //IO13
    private Gpio A4;    //IO14

    //private Gpio CLS;
    //private Gpio CLK;
    private Gpio WR;    //IO15
    private Gpio RD;    //IO16
    private Gpio CE;    //IO17
    private PeripheralManagerService service;
    private loopRunner looper;
    private int systemBrightnes = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        service = new PeripheralManagerService();

        try {
            setupGPIOS();
            //WRITE_Character_Ram(1,23);
            looper = new loopRunner(this);
            looper.run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void selfTest() throws IOException,InterruptedException{
        Control_Register(100,false,false,false,false);
        sleep(1);
        Log.d(TAG, "selfTest tarted");
        Control_Register(100,false,false,true,false);
        sleep(5000);
        Log.d(TAG, "selfTest finided");
        clearDisplay();

        WRITE_Character_Ram(1,23);
        WRITE_Character_Ram(2,23);
        WRITE_Character_Ram(3,23);
        WRITE_Character_Ram(4,23);
        WRITE_Character_Ram(5,23);
        WRITE_Character_Ram(6,23);
        WRITE_Character_Ram(7,23);
        WRITE_Character_Ram(8,23);
        sleep(5000);
        clearDisplay();
    }

    private void clearDisplay() throws IOException, InterruptedException {
        Control_Register(systemBrightnes,false,false,false,true);
        sleep(1);
        Control_Register(systemBrightnes,false,false,false,false);
        sleep(1);
    }
    
    private void setupGPIOS()throws IOException{

            D0= configureOutput("IO2");
            D1= configureOutput("IO3");
            D2= configureOutput("IO4");
            D3= configureOutput("IO5");
            D4= configureOutput("IO6");
            D5= configureOutput("IO7");
            D6= configureOutput("IO8");
            D7= configureOutput("IO9");

            A0= configureOutput("IO10");
            A1= configureOutput("IO11");
            A2= configureOutput("IO12");
            A3= configureOutput("IO13");
            A4= configureOutput("IO14");

            //private Gpio CLS;
            //private Gpio CLK;
            WR= configureOutput("IO15");
            RD= configureOutput("IO16");
            CE= configureOutput("IO17");


            CE.setValue(false);
            WR.setValue(false);
            RD.setValue(false);
    }

    boolean isSet(byte value, int bit){
        return (value&(1<<bit))==0;
    }


    private void  WRITE_Character_Ram(int posation,int CODE, boolean... UDCFlag) throws IOException, InterruptedException {
        boolean UDC = (UDCFlag.length >= 1) ? true : false;

        A4.setValue(false);
        A3.setValue(false);
        setCharAddress(posation-1);

        if(UDC){
            D7.setValue(false);
        }else {
            D7.setValue(true);
        }

        byte code = (byte)CODE;

        // D3 D2 D1 D0
        // 1  0  1  0 Row A
        D0.setValue(isSet(code,0));
        D1.setValue(isSet(code,1));
        D2.setValue(isSet(code,2));
        D3.setValue(isSet(code,3));
        //D6 D5 D4
        //0  1  0 COLUMN 2

        D4.setValue(isSet(code,4));
        D5.setValue(isSet(code,5));
        D6.setValue(isSet(code,6));

        writeToDisplay();

    }

    private void writeToDisplay() throws IOException, InterruptedException {
        //Log.d(TAG, "writeToDisplay");
        CE.setValue(true);
        sleep(1);
        WR.setValue(true);
        RD.setValue(false);
        sleep(1);
        WR.setValue(false);
        sleep(1);
        CE.setValue(false);
        sleep(1);

        CE.setValue(false);
        WR.setValue(false);
        RD.setValue(false);
    }

    private void setCharAddress(int character_address)throws IOException {
        A0.setValue(isSet((byte)character_address,0));
        A1.setValue(isSet((byte)character_address,1));
        A2.setValue(isSet((byte)character_address,2));
    }


    private void set_BRIGHTNESS(int BRIGHTNESS)throws IOException {

        byte brit = (byte)((BRIGHTNESS/100)*8);
        D0.setValue(!isSet((byte)brit,0));
        D1.setValue(!isSet((byte)brit,1));
        D2.setValue(!isSet((byte)brit,2));
        /*
        if(BRIGHTNESS>90) {                             //1000%
                D2.setValue(true);
                D1.setValue(true);
                D0.setValue(true);
        }else if(BRIGHTNESS < 90 && BRIGHTNESS > 66 ){  //80%
                D2.setValue(true);
                D1.setValue(true);
                D0.setValue(false);
        }else if(BRIGHTNESS < 66 && BRIGHTNESS > 46 ){      //53%
                D2.setValue(true);
                D1.setValue(false);
                D0.setValue(true);
        }else if(BRIGHTNESS < 46 && BRIGHTNESS > 33 ){      //40%
                D2.setValue(true);
                D1.setValue(false);
                D0.setValue(false);
        }else if(BRIGHTNESS < 33 && BRIGHTNESS > 23.5 ){    //27%
                D2.setValue(false);
                D1.setValue(true);
                D0.setValue(true);
        }else if(BRIGHTNESS < 23.5 && BRIGHTNESS > 16.5 ){  //20%
                D2.setValue(false);
                D1.setValue(true);
                D0.setValue(false);
        }else if(BRIGHTNESS < 16.5 && BRIGHTNESS > 6.5 ){   //13%
                D2.setValue(false);
                D1.setValue(false);
                D0.setValue(true);
        }else if(BRIGHTNESS < 6.5) { //0%
                D2.setValue(false);
                D1.setValue(false);
                D0.setValue(false);
        }
        */
    }

    private void  WRITE_User_Defined_Character(int CHARACTER_ADDRESS,int CODE,int ROW_SELECT,int DOT_DATA)throws IOException{

    }

    private void  Control_Register(int Brightness,boolean Flash, boolean Blink, boolean Self_Test, boolean Clear) throws IOException, InterruptedException {

        //CONTROL WORD ADDRESS
        A4.setValue(false);
        A3.setValue(true);
        //A2.setValue(true);
        //A1.setValue(true);
        //A0.setValue(true);

        //Brightness
        set_BRIGHTNESS(Brightness);

        /*
        Bit 3 determines whether the flashing character attribute
        is on or off. When bit 3 is a“1,”the output of the Flash RAM
        is checked. If the content of a loca­tion in the Flash RAM is
        a “1,”the associated digit will flash at approximately 2 Hz.
        For an external clock, the blink rate can be calculated by
        dividing the clock frequency by 28,672. If the flash enable
        bit of the Control Word is a “0,” the content of the Flash
        RAM is ignored. To use this function with multiple dis­play
        systems see the Reset section.
         */
        D3.setValue(!Flash);

        /*
        Bit 4 of the ControlWord is used to synchronize blinking of
        all eight digits of the display. When this bit is a “1” all eight
        digits of the display will blink at approxi­mately 2 Hz. The
        actual rate is dependent on the clock frequency. For an
        external clock, the blink rate can be calculated by dividing
        the clock frequency by 28,672. This function will override
        the Flash function when it is active. To use this function
        with multiple display systems see the Reset section.
         */
        D4.setValue(!Blink);


        //D5.setValue(false);
        /*
        Bit 6 of the ControlWord Regis­ter is used to initiate the self
        test function. Results of the internal self test are stored in
        bit 5 of the Control Word. Bit 5 is a read only bit where bit
        5 =“1”indicates a passed self test and bit 5 =“0”indicates
        a failed self test.
         */
        D6.setValue(!Self_Test);


        /*
        Bit 7 of the Control Word will clear the Character RAM
        and the Flash RAM. Setting bit 7 to a “1” will start the
        clear func­tion. Three clock cycles (110 ms min. using the
        internal refresh clock) are required to complete the clear
        function. The display must not be accessed while the
        display is being cleared. When the clear function has been
        com­pleted, bit 7 will be reset to a “0.” The ASCII char­acter
        code for a space (20H) will be loaded into the Character
        RAM to blank the display and the Flash RAM will be loaded
        with“1”s. The UDC RAM, UDC Address Register and the remainder
        of the Control Word are unaffected.
         */
        D7.setValue(!Clear);

        writeToDisplay();
    }






    private GpioCallback mGpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            // Read the active low pin state
            // Continue listening for more interrupts
            return true;
        }

        @Override
        public void onGpioError(Gpio gpio, int error) {
            Log.w(TAG, gpio + ": Error event " + error);
        }
    };


    public Gpio configureOutput(String Name) throws IOException {
        Gpio gpio = service.openGpio(Name);
        // Initialize the pin as a high output
        gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        // Low voltage is considered active
        gpio.setActiveType(Gpio.ACTIVE_LOW);
        return gpio;
    }


    public Gpio configureInPut(String Name) throws IOException {
        Gpio gpio = service.openGpio(Name);
        // Initialize the pin as a high output
        gpio.setDirection(Gpio.DIRECTION_IN);
        // Low voltage is considered active
        gpio.setActiveType(Gpio.ACTIVE_LOW);
        return gpio;
    }


    public class loopRunner implements Runnable {
        private final MainActivity activity;
        loopRunner(MainActivity activity){
            this.activity = activity;

        }

        @Override
        public void run() {
            try {
                activity.selfTest2();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void selfTest2() throws IOException, InterruptedException {

        systemBrightnes = 4;
        clearDisplay();
        WRITE_Character_Ram(1,0);
        WRITE_Character_Ram(2,0);
        WRITE_Character_Ram(3,0);
        WRITE_Character_Ram(4,'A');
        WRITE_Character_Ram(5,'B');
        WRITE_Character_Ram(6,'C');

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
