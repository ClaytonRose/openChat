/*
 * OpenChat
 * March 2014
 * Clayton Rose
 */

package cr.clayton.openchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity {
	private static final int PORT = 4451;
	static private BufferedReader bufferedReader;
	static private Socket client;
	static private PrintWriter printwriter;
	TextView convoTextView;
	int counter = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Connect to server
		new AsyncSetup().execute();
		
		//Start Listening
		new AsyncListen().execute();
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	
	// Called when user clicks send button
	public void sendMessage(View view){

		//Need this to hide keyboard
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);

	    EditText editText = (EditText) findViewById(R.id.editTextMessage);
	    String message = editText.getText().toString();
	    convoTextView = (TextView) findViewById(R.id.textView1);
	    
	    // Allow scrolling
	    convoTextView.setMovementMethod(new ScrollingMovementMethod());
		
	    //Make sure the message is valid
	    if(messageValid(message)){
	    	
	    	//append message to the conversation
	    	convoTextView.append(message + "\n");
	       	convoTextView.requestFocus();
	       	
	       	//Send Message
	       	Log.d("dd", "About to async write");
			new AsyncWrite().execute(message);
	       	
	       	// Hide the keyboard
	       	imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	    }
	    
	    //clear the text field
		editText.setText("");
	}
	
	//Used to post a newly received message to the textview
	public void recieveMessage (String messageFromServer){
		convoTextView.append(messageFromServer + "/n");
	}
	
	// Checks a message before sending
	public boolean messageValid(String message){
		if(message.isEmpty()){
			return false;
		} else {
			return true;
		}
	}
	
	// Writes a message to the server
	private class AsyncWrite extends AsyncTask<String, Void, Void> {

		protected Void doInBackground(String...args) {
			Log.d("dd", "SENDING MESSAGE " + args[0]);
			printwriter.write(args[0]);
			printwriter.flush();
			
			return null;
		}

	}
	
	// Sets up the connections
	private class AsyncSetup extends AsyncTask<String, Void, Void> {
		//String messageFromServer;
		InputStreamReader inputStreamReader;
		
		protected Void doInBackground(String... args) {
			Log.d("dd", "In async setup");
			try {
				// Connect to server
				client = new Socket("192.168.56.1", PORT);
				printwriter = new PrintWriter(client.getOutputStream(), true);
				inputStreamReader = new InputStreamReader(client.getInputStream());
				bufferedReader = new BufferedReader(inputStreamReader);

			} catch (UnknownHostException e) {
				e.printStackTrace();

			} catch (IOException e) {
				e.printStackTrace();

			}

			return null;
		}
	}
	
	// Listens to server
	private class AsyncListen extends AsyncTask<String, Void, Void>{

		protected Void doInBackground(String... params) {
			String messageFromServer;
			Log.d("dd", "LISTENING");
			// Start Reading
			while(true){
				try {
					messageFromServer = bufferedReader.readLine();
					Log.d("dd", "recieved message: " + messageFromServer);
					recieveMessage(messageFromServer);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
}

