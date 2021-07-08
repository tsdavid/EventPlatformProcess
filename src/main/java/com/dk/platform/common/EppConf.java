package com.dk.platform.common;

import com.dk.platform.common.confs.EMS_Conf;
import com.dk.platform.common.confs.MessageConf;
import com.dk.platform.common.confs.ProcessConf;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

@Slf4j
public class EppConf {

    final String PlatformName = "Name"; private String PlatformNameVal;
    final String Enviroment= "Env"; private String EnviromentVal;
    final String EMS = "EMS"; final String Message = "Message"; final String Process = "Process";

    public EMS_Conf ems; public MessageConf message; public ProcessConf process;

    private JSONObject jsonObject;

    public EppConf(){

        // TODO TODO REMOVE.

    }


    public EppConf(String filePath){


        try {
            JSONParser jsonParser = new JSONParser();
            jsonObject = (JSONObject) jsonParser.parse(new FileReader(filePath));

            PlatformNameVal = (String) jsonObject.get(PlatformName);

            EnviromentVal = (String) jsonObject.get(Enviroment);

            JSONObject EnvObject = (JSONObject) jsonObject.get(EnviromentVal);
            ems = new EMS_Conf((JSONObject) EnvObject.get(EMS));

            message = new MessageConf((JSONObject) EnvObject.get(Message));

            process = new ProcessConf((JSONObject) EnvObject.get(Process));

        } catch (IOException e) {
            e.printStackTrace();

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }



    public static void main(String[] args) {


        System.out.println(new EppConf().ems.toString());
        System.out.println(new EppConf().message.toString());
        System.out.println(new EppConf().process.toString());
    }

}

