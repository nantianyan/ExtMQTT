package com.example.cloudmedia;

/**
 * Created by 阳旭东 on 2017/10/19.
 */

/**
 * this class is used to config cloud live server
 * the following feature may be took into consideration:
 * record
 * transcode
 */
public class LiveServerNode {
    /**
     * ask peer node to recode video during push live streaming
     * @param params
     * @return
     */
    public boolean configLiveVideoEncoder(String params){
        return true;
    }
}
