package com.amazonaws.kinesisvideo.demoapp.constant;

public class Constants {
    public interface ACTION {
        public static String NOTIFICATION_BAR_ACTION = "com.amazonaws.kinesisvideo.demoapp.motiondetection.action.notificationbar";
        public static String STARTFOREGROUND_ACTION = "com.amazonaws.kinesisvideo.demoapp.motiondetection.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.amazonaws.kinesisvideo.demoapp.motiondetection.action.stopforeground";

        public static String MOTION_DETECTED_ACTION = "com.amazonaws.kinesisvideo.demoapp.motiondetection.action.motiondetected";

        public static String NOTIFICATION_TOKEN_RECEIVED_ACTION = "com.amazonaws.kinesisvideo.demoapp.firebasecloudmessage.action.tokenreceived";
        public static String NOTIFICATION_MSG_RECEIVED_ACTION = "com.amazonaws.kinesisvideo.demoapp.firebasecloudmessage.action.messagereceived";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public interface NOTIFICATION_COMMAND {
        public static String START_STREAMING = "startstreaming";
        public static String STOP_STREAMING = "stopstreaming";
        public static String START_ARMING = "startarming";
        public static String STOP_ARMING = "stoparming";
    }
}