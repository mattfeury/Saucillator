
/** 
 * Play a file by streaming off of disk.
 * Output the synthesized data using SampleQueueOutputStream
 *
 * @author (C) 2003 Phil Burk, All Rights Reserved
 */

import java.applet.Applet;
import java.awt.Button;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import com.softsynth.jsyn.*;
import com.softsynth.jsyn.util.SampleQueueOutputStream;
import com.softsynth.jsyn.view11x.PortFader;

class SampleFileStreamer extends SynthCircuit implements Runnable {
	public SynthSample mySamp;
	SampleReader mySampler;
	final static int FRAMES_PER_BLOCK = 400;
	// number of frames to synthesize at one time
	final static int FRAMES_IN_BUFFER = 8 * 1024;
	int numChannels = 1;
	int samplesPerBlock;
	short[] data;
	SampleQueueOutputStream outStream;
	Thread thread;
	SynthInput amplitude;
	File sampleFile;
	InputStream inStream;
	int numFrames;
	int frameCursor;

	/** Create a streamer for the specified file.
	 * File header will be read for info needed for streaming.
	 */
	public SampleFileStreamer(File file) throws IOException {
		super();
		sampleFile = file;
		InputStream stream = new FileInputStream(file);
		setup(stream);
	}

	/** Analyse sample header and create appropriate player. */
	private void setup(InputStream stream) throws IOException {
		BufferedInputStream bufStream = new BufferedInputStream(stream);
		mySamp = new SynthSampleWAV();

		// Preload to get offset to data and other sample info.
		// The false flag means do not load the actual WAV data at this time.
		mySamp.load(bufStream, false);
		mySamp.dump();

		bufStream.close();

		numChannels = mySamp.getChannelsPerFrame();
		numFrames = mySamp.getNumFrames();

		//	Create SynthUnits to play sample data.
		if (numChannels == 1) {
			mySampler = new SampleReader_16F1();
		} else if (numChannels == 2) {
			mySampler = new SampleReader_16F2();
		} else {
			throw new RuntimeException("This example only supports mono or stereo!");
		}
		add(mySampler);

		samplesPerBlock = FRAMES_PER_BLOCK * numChannels;
		data = new short[samplesPerBlock];
		addPort(amplitude = mySampler.amplitude);

		// Create a stream that we can write to.
		outStream =
			new SampleQueueOutputStream(
				mySampler.samplePort,
				FRAMES_IN_BUFFER,
				numChannels);
	}

	public SynthOutput getOutput() {
		return mySampler.output;
	}

	/** Thread task that plays a file from disk. */
	public void run() {
		try {
			try {
				while (thread != null) {
					if( sendBuffer() <= 0 ) break;
				}
			} finally {
				outStream.flush();
				inStream.close();
			}
		} catch (IOException e) {
			System.out.println("run() caught " + e);
		} catch (SynthException e) {
			System.out.println("run() caught " + e);
		}
	}

	/** Read data from file and write it to the audio output stream.
	 * @return framesLeft;
	 */
	int sendBuffer() throws IOException {
		int samplesToRead = samplesPerBlock;
		int framesLeft = numFrames - frameCursor;
		int samplesLeft = framesLeft * numChannels;
		if (samplesToRead > samplesLeft)
			samplesToRead = samplesLeft;
		int i = 0;

		// Read sample data from file as bytes and assemble into 16 bit samples.
		while (i < samplesToRead) {
			int sample = inStream.read(); // get LSB
			if (sample < 0) {
				throw new IOException("Premature EOF");
			}
				sample = (inStream.read() << 8) // MSB
	| (sample & 0x00FF);
			data[i++] = (short) sample;
		}

		if (i > 0) {
			int framesRead = i / numChannels;
			frameCursor += framesRead;
			// Write data to the stream.
			// Will block if there is not enough room so run in a thread.
			outStream.write(data, 0, framesRead);
		}
		return framesLeft;
	}

	/** Start playing the stream in the background thread. */
	void startStream() {
		try {
			FileInputStream fileStream = new FileInputStream(sampleFile);
			inStream = new BufferedInputStream(fileStream);

			// Skip to where the sample data starts.
			inStream.skip(mySamp.getOffset());

			frameCursor = 0;
			// Prefill output stream buffer so that it starts out full.
			while (outStream.available() > FRAMES_PER_BLOCK)
				sendBuffer();
			// Start slightly in the future so everything is synced.
			int time = Synth.getTickCount() + 4;
			mySampler.start(time);
			outStream.start(time);

			// launch a thread to keep stream supplied with data
			thread = new Thread(this);
			thread.start();
		} catch (IOException exc) {
			System.err.println(exc.toString());
		}
	}

	/** Stop playing the stream and wait for data to flush. */
	void stopStream() {
		Thread myThread = thread;
		if (thread != null) {
			thread = null; // tells thread to stop!
			try {
				myThread.join(1000);
			} catch (InterruptedException e) {
			}
			int time = Synth.getTickCount();
			outStream.stop(time);
			mySampler.stop(time);
		}
	}
}

