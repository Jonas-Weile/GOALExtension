package org.eclipse.gdt;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.core.DLTKCore;

public class Metrics {
	public enum Event {
		RUN_MAS, DEBUG_MAS, RUN_TEST, DEBUG_TEST, STEP_IN, STEP_OVER, STEP_OUT, BREAKPOINT, HISTORY_STEP, END
	}

	private final static Queue<Metric> metrics = new ConcurrentLinkedQueue<>();
	private final static String url = "http://insyprojects.ewi.tudelft.nl/vincent.php";

	// CREATE TABLE VincentKoeman.metrics (
	// uuid VARCHAR(36) NOT NULL,
	// timestamp INT(11) UNSIGNED NOT NULL,
	// event VARCHAR(18) NOT NULL,
	// PRIMARY KEY (uuid, timestamp)
	// ) CHARACTER SET utf8 COLLATE utf8_bin;
	//
	// INSERT INTO metrics (uuid,timestamp,event) VALUES (?,?,?);

	private Metrics() {
	} // static class

	public static void event(final Event event) {
		// final String uuid =
		// Activator.getDefault().getPreferenceStore().getString(Activator.PERMISSION);
		// if (uuid.length() > 1) { // not empty and not 'N'
		// metrics.add(new Metric(uuid, event));
		// }
	}

	public static class MetricPersistJob extends Job {
		private boolean running = true;

		public MetricPersistJob() {
			super("Metric Persist Job");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Metric metric;
			while ((metric = metrics.poll()) != null) {
				try {
					final URL post = new URL(url);
					final byte[] data = metric.getData().getBytes();
					final HttpURLConnection conn = (HttpURLConnection) post.openConnection();
					conn.setDoOutput(true);
					conn.setInstanceFollowRedirects(false);
					conn.setUseCaches(false);
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					conn.setRequestProperty("Content-Length", Integer.toString(data.length));
					conn.getOutputStream().write(data);
					conn.getResponseCode();
				} catch (final Exception e) {
					DLTKCore.error(e);
				}
			}
			schedule(60000); // 1 minute
			return Status.OK_STATUS;
		}

		@Override
		public boolean shouldSchedule() {
			return this.running;
		}

		public void stop() {
			this.running = false;
		}
	}

	private static class Metric {
		private final String uuid;
		private final String event;
		private final int timestamp;

		Metric(final String uuid, final Event event) {
			this.uuid = uuid;
			this.event = event.name();
			this.timestamp = (int) (System.currentTimeMillis() / 1000L);
		}

		String getData() {
			return "uuid=" + this.uuid + "&timestamp=" + this.timestamp + "&event=" + this.event;
		}
	}
}
