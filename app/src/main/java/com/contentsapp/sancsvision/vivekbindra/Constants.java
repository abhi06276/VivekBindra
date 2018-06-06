package com.contentsapp.sancsvision.vivekbindra;

/**
 * Created by abhinandan on 01/06/18.
 */

public class Constants {

    public static Environments currentEnv = Environments.NON_PROD;
    public static int contentOwnerId = 1;

    public static final String DEVELOPER_KEY = "AIzaSyABYoczeHg4XABx_jMRfv-CqmA2YMsIY4A";

    // YouTube video id
    public static final String YOUTUBE_VIDEO_CODE = "_oEA18Y8gM0";



    public static String BASE_URL;



     static {
        if(currentEnv == Environments.NON_PROD){
            BASE_URL = "https://sheltered-plateau-11813.herokuapp.com/";
        }
        else{
            BASE_URL = "https://sheltered-plateau-11813.herokuapp.com/";
        }
    }

    //URLS
    public static String GET_CONTENTS_LIST ="content/"+contentOwnerId ;




}
enum Environments  {
    PROD ,
    NON_PROD


}
