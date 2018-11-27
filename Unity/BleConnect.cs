using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BleConnect : MonoBehaviour {
    public AndroidJavaClass unityPlayer;
    public AndroidJavaObject currentActivity;
    public AndroidJavaObject sysService;
	public TextMesh testtext;
	public TextMesh testtext2;
	// Use this for initialization
	void Start () {
		#if UNITY_ANDROID
        AndroidJavaClass application = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        currentActivity = application.GetStatic<AndroidJavaObject>("currentActivity");
        sysService = new AndroidJavaClass("com.wistron.gerry.bleconnect.androidbleconnect");
		currentActivity.Call("blesttart3");
        //int K = sysService.CallStatic<int>("blestart");
		//testtext2.text = K.ToString();
		//testtext.text = K.ToString();
        //IARAppFunction("123");
        //currentActivity.Call("StartListening"); 
        //currentActivity.Call("OpenSpeechBtn");
		#endif
	}
	
	// Update is called once per frame
	void Update () {
		
	}

	public void message(string connectstring){
		testtext.text = connectstring;
	}

	
}
