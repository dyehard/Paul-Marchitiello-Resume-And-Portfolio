//package Assignment 4;

import java.io.IOException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.io.BufferedWriter;
import java.io.FileWriter;

import java.util.ArrayList;

public class Assignment4{

    public static void main(String[] args){
        VirtualMemorySimulation VirtualMemorySimulation = new VirtualMemorySimulation();
    }
}

class VirtualMemorySimulation{
    //Text file paths
    private String rootFilePath = "G:\\Repos_New\\CSC-139\\Assignment 4"; //Update the root file path for your input and output files
    private String inputFilePath = rootFilePath + "\\input.txt";
    private String outputFilePath = rootFilePath + "\\output.txt";
    private File inputFile;

    //Automated Testing: To use automated testing your test input/output files must be in the format 'testN.txt' and 'testNo.txt' respectively. Where 'N' is the test number.
    //Step 1: Set the enableAutomatedTesting boolean to true.
    //Step 2: Set the testCount int to the number of test input files you would like to run.
    //Step 3: Run the program
    //Done: You can review the test results in the console. Results show output vs expected preceeded by an ERROR if they don't match. The total errors for all tests displays last. 
    private boolean enableAutomatedTesting = false; //Set to true to enable automated testing 
    private int testCount = 5; // Set this value equal to the number of test files
    
    private int testNumber = 1;
    private String testInputFilePath;
    private String testOutputFilePath;
    private int errorCount = 0;
    private int errorTotal = 0;

    private int[] requests;

    private int[][] frames;

    private int pageFaultCount = 0;

    private ArrayList<String> log = new ArrayList<String>();

    private int fifoIndex = 0;

    public static void main(String[] args){
        
    }

    public VirtualMemorySimulation(){

        if(enableAutomatedTesting){
            
            for (int i = 1; i <= testCount; i++){
                testNumber = i;
                testInputFilePath = rootFilePath + "\\test" + i + ".txt";
                testOutputFilePath = rootFilePath + "\\test" + i + "o.txt";
                ReadInputFile();
            }
        }
        else
            ReadInputFile();
    }

    public void ReadInputFile(){ 

        try{
            
            if(enableAutomatedTesting){
                inputFile = new File(testInputFilePath);
            }
            else
                inputFile = new File(inputFilePath);
            
            Scanner myReader = new Scanner(inputFile);
            
            String[] fistLineStrings = myReader.nextLine().split("\\s+");
            
            frames = new int[Integer.parseInt(fistLineStrings[1])][2];
            requests = new int[Integer.parseInt(fistLineStrings[2])];

            ResetVariables();

            for (int i = 0; i < requests.length; i++){
                String[] nextLine = myReader.nextLine().split("\\s+");
                requests[i] = Integer.parseInt(nextLine[0]);
            }
            
            myReader.close();
        
            FIFO();
            Optimal();
            LRU();

            if(enableAutomatedTesting){
                
                TestCode();
                System.out.println("Total Errors: " + errorTotal);
                log = new ArrayList<String>();
            }
            
        }
        catch (FileNotFoundException e){
            System.out.println("File not found. Did you update the rootFilePath variable on line 23?");
            e.printStackTrace();
        }
    }

    public void WriteOutputFile(){

        try{
            BufferedWriter outputFile = new BufferedWriter(new FileWriter(outputFilePath));

            for(int i = 0; i < log.size(); i++){
                outputFile.write(log.get(i));
                outputFile.newLine();
            }

            outputFile.close();
        }
        catch (IOException e ){
            System.out.println("File not found. Did you update the rootFilePath variable on line 23?");
            e.printStackTrace();
        }

        errorTotal += errorCount;
    }

    public void ResetVariables(){
        for(int i = 0; i < frames.length; i++){
            frames[i][0] = -1;
        }

        pageFaultCount = 0;
        errorCount = 0;
    }

    public boolean CheckIfPageLoaded(int page, int currentTime ){
        for (int i = 0; i < frames.length; i++)
        {
            if(frames[i][0] == page){
                log.add("Page " + page + " already in Frame " + i);
                frames[i][1] = currentTime;
                return true;
            }
        }
        pageFaultCount++;
        return false;
    }

    public boolean CheckIfFrameIsEmpty(int page, int currentTime){

        for (int i = 0; i < frames.length; i++)
        {
            if(frames[i][0] == -1 ){
                frames[i][0] = page;
                frames[i][1] = currentTime;
                log.add("Page " + page + " loaded into Frame " + i);
                return true;
            }
        }

        return false;
    }

    public void TestCode(){

        ArrayList<String> correctOutput = new ArrayList<String>();

        try{
            File testfile = new File(testOutputFilePath);
            Scanner myReader = new Scanner(testfile);
            
            while (myReader.hasNext()) {
                correctOutput.add(myReader.nextLine());
            }
            
            myReader.close();
        }
        catch (FileNotFoundException e){
            System.out.println("Test file not found!");
            e.printStackTrace();
        }

        System.out.println("Test" + testNumber );

        for ( int i = 0; i < log.size(); i++ ){
            if( !log.get(i).equalsIgnoreCase(correctOutput.get(i))){
                System.out.println("ERROR!!!!!!");
                errorCount++;
            }
            System.out.println("MyOutput: " + log.get(i));
            System.out.println("Expected: " + correctOutput.get(i));
        }
        System.out.println("ERRORS: " + errorCount );
    }

    public void FIFO(){

        ResetVariables();
        fifoIndex = 0;
        log.add("FIFO");

        for (int i = 0; i < requests.length; i++)
        { 
            if (!CheckIfPageLoaded(requests[i], i) && !CheckIfFrameIsEmpty(requests[i], i)){

                log.add("Page " + frames[fifoIndex][0] + " unloaded from Frame " + fifoIndex 
                + ", Page " + requests[i] + " loaded into Frame " + fifoIndex);
                frames[fifoIndex][0] = requests[i];
                
                fifoIndex++;
                if (fifoIndex >= frames.length)
                    fifoIndex = 0;
            }
        }

        log.add(pageFaultCount + " page faults");
        WriteOutputFile();
    }

    public void Optimal(){

        ResetVariables();

        log.add("");
        log.add("Optimal");

        for (int i = 0; i < requests.length; i++)
        { 
            if (!CheckIfPageLoaded(requests[i], i) && !CheckIfFrameIsEmpty(requests[i], i)){


                ArrayList<Integer> tempFrames = new ArrayList<Integer>();
                //Copy frames into tempFrames
                for(int j = 0; j < frames.length; j++)
                    tempFrames.add(frames[j][0]);

                //Look into the future
                for (int p = i + 1; p < requests.length; p++){
                     
                    if(tempFrames.size() == 1)
                    {
                        break;
                    }

                    int target = tempFrames.indexOf(requests[p]);
                    if(target != -1)
                        tempFrames.remove(target);
                }

                //Get the index of the frame to unload and handle unload
                for (int k = 0; k < frames.length; k++){

                    if (frames[k][0] == tempFrames.get(0)){
                        
                        log.add("Page " + frames[k][0] + " unloaded from Frame " + k 
                            + ", Page " + requests[i] + " loaded into Frame " + k);
                        
                        frames[k][0] = requests[i];
                    }
                } 
            }
        }

        log.add(pageFaultCount + " page faults");
        WriteOutputFile();
    }

    public void LRU(){
        ResetVariables();

        log.add("");
        log.add("LRU");

        for (int i = 0; i < requests.length; i++)
        { 
            if (!CheckIfPageLoaded(requests[i], i) && !CheckIfFrameIsEmpty(requests[i], i)){

                int[] leastRecentlyUsedIndex = new int[2];
                leastRecentlyUsedIndex[0] = 0;
                leastRecentlyUsedIndex[1] = i;
                for (int p = 0; p < frames.length; p++){
                    
                    if (frames[p][1] < leastRecentlyUsedIndex[1]){
                        leastRecentlyUsedIndex[0] = p;
                        leastRecentlyUsedIndex[1] = frames[p][1];
                    }
                }

                log.add("Page " + frames[leastRecentlyUsedIndex[0]][0] + " unloaded from Frame " + leastRecentlyUsedIndex[0] 
                + ", Page " + requests[i] + " loaded into Frame " + leastRecentlyUsedIndex[0]);
                        
                frames[leastRecentlyUsedIndex[0]][0] = requests[i];
                frames[leastRecentlyUsedIndex[0]][1] = i;
            }
        }

        log.add(pageFaultCount + " page faults");
        WriteOutputFile();
    }
} 