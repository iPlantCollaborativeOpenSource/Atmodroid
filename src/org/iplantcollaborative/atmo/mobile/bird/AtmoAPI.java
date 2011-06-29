package org.iplantcollaborative.atmo.mobile.bird;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class AtmoAPI implements Parcelable {
	private static final boolean MOBILE = true;
	private static final boolean DEBUG = false;
	private static final String TAG = "AtmoDroid";
	private String myserver;
	private Date validdate;
	private HashMap<String, String> credentials;
	private HashMap<String, AtmoImage> images;
	private HashMap<String, AtmoInstance> instances;
	private HashMap<String, String> keypairs;
	private HashMap<String, AtmoApp> apps;
	private HashMap<String, AtmoVolume> volumes;
	private String registrationID;

	public AtmoAPI(String myserver) {
		validdate = null;
		this.myserver = myserver;
		credentials = new HashMap<String, String>();
	}

	/********************** GET/SET ******************************/
	protected String getServer() {
		return credentials.get("X-Api-Server");
	}

	protected String getToken() {
		return credentials.get("X-Auth-Token");
	}

	public String getUser() {
		return credentials.get("X-Auth-User");
	}

	protected String getVersion() {
		return credentials.get("X-Api-Version");
	}

	protected void setDate(Date d) {
		validdate = d;
	}

	public void setCredentials(String server, String token, String user,
			String version) {
		this.credentials = new HashMap<String, String>();
		this.credentials.put("X-Api-Server", server);
		this.credentials.put("X-Auth-Token", token);
		this.credentials.put("X-Auth-User", user);
		this.credentials.put("X-Api-Version", version);
	}

	public HashMap<String, AtmoApp> getApps() {
		authIfNotValid();
		apps = new HashMap<String, AtmoApp>();
		readAppList();
		return apps;
	}

	public AtmoApp getApp(String key) {
		if (apps == null)
			getApps();
		return apps.get(key);
	}

	public Set<String> getAppList() {
		if (apps == null)
			getApps();
		return apps.keySet();
	}

	public HashMap<String, AtmoImage> getImages() {
		// Get request: No parameters
		authIfNotValid();
		images = new HashMap<String, AtmoImage>();
		readImages();
		return images;
	}

	public AtmoImage getImage(String key) {
		if (images == null)
			getImages();
		return images.get(key);
	}

	public Set<String> getImageList() {
		if (images == null)
			getImages();
		return (Set<String>) images.keySet();
	}

	/**
	 * VM List: The list of all instances and apps
	 */
	public HashMap<String, AtmoInstance> getInstances() {
		authIfNotValid();
		instances = new HashMap<String, AtmoInstance>();
		readInstances();
		return instances;
	}

	public AtmoInstance getInstance(String instance) {
		if (instances == null)
			getInstances();
		return instances.get(instance);
	}

	public Set<String> getInstanceList() {
		if (instances == null)
			getInstances();
		return (Set<String>) instances.keySet();
	}

	public HashMap<String, AtmoInstance> getRunningInstances() {

		HashMap<String, AtmoInstance> map = getInstances();
		HashMap<String, AtmoInstance> nmap = new HashMap<String, AtmoInstance>();
		AtmoInstance ai;
		String testname = "";
		int prefix;
		// Trim instances that are not running.
		for (String iname : map.keySet()) {
			ai = map.get(iname);
			if (ai.getInstance_state().equals("running") != false) {
				String strname = ai.getName();
				testname = strname;
				prefix = 2;
				while (nmap.get(testname) != null) {
					testname = strname + " (" + prefix + ")";
					prefix++;
				}
				nmap.put(testname, ai);
			}
		}
		return nmap;
	}

	public Set<String> getRunningInstanceList() {
		HashMap<String, AtmoInstance> map = getRunningInstances();
		return (Set<String>) map.keySet();
	}

	public HashMap<String, AtmoInstance> getPendingInstances() {
		HashMap<String, AtmoInstance> map = getInstances();
		HashMap<String, AtmoInstance> nmap = new HashMap<String, AtmoInstance>();
		AtmoInstance ai;
		for (String iname : map.keySet()) {
			ai = map.get(iname);
			if (ai.getInstance_state().equals("pending") == true) {
				nmap.put(iname, ai);
			}
		}
		return nmap;
	}

	public Set<String> getPendingInstanceList() {
		HashMap<String, AtmoInstance> map = getPendingInstances();
		return (Set<String>) map.keySet();
	}

	public HashMap<String, String> getKeyPairs() {
		authIfNotValid();
		keypairs = new HashMap<String, String>();
		readKeyPairs();
		return keypairs;
	}

	public Set<String> getKeyPairList() {
		if (keypairs == null)
			getKeyPairs();
		return (Set<String>) keypairs.keySet();
	}

	public HashMap<String, AtmoVolume> getVolumes() {
		authIfNotValid();
		volumes = new HashMap<String, AtmoVolume>();
		readVolumes();
		return volumes;
	}

	public Set<String> getVolumeList() {
		if (volumes == null)
			getVolumes();
		return (Set<String>) volumes.keySet();
	}

	public AtmoVolume getVolume(String volume) {
		if (volumes == null)
			getVolumes();
		return volumes.get(volume);
	}

	/********************** APPS ******************************/

	private void readAppList() {
		// Get request: No parameters
		try {
			String msg = atmo_GET_to_JSON("getAppList");
			// Parse JSON
			JSONObject orig = (JSONObject) new JSONTokener(msg).nextValue();
			// Result Object
			JSONObject object = orig.getJSONObject("result");
			// Values (Where images are stored)
			JSONArray value = object.getJSONArray("value");
			JSONObject temp;
			AtmoApp aa;
			String test, base;
			int prefix;
			/* Iterate apps, save */
			for (int i = 0; i < value.length(); i++) {
				temp = value.getJSONObject(i);
				test = base = temp.getString("application_name");
				prefix = 2;
				while (apps.get(test) != null) {
					test = base + " (" + prefix++ + ")";
				}
				aa = new AtmoApp(temp.getString("application_type"),
						temp.getString("platform"),
						temp.getString("application_created"), test,
						temp.getString("application_icon_path"),
						temp.getString("machine_image_id"),
						temp.getString("application_creator"),
						temp.getString("application_tags"),
						temp.getString("application_description"),
						temp.getString("ramdisk_id"),
						temp.getString("system_minimum_requirements"),
						temp.getString("application_category"),
						temp.getString("kernel_id"),
						temp.getString("application_version"),
						temp.getString("application_id"),
						temp.getBoolean("is_sys_app"));
				apps.put(test, aa);

			}
		} catch (Exception e) {
			;
		}
	}

	public String launchApp(String name, AtmoApp aa, String keypair) {
		String urlex = "launchApp";
		String callback_url = "http://quentin.iplantcollaborative.org/c2dm/C2DM.php?user="+getUser()+"&msg=TEST";
		try {
			String params = "image_id="
				+ URLEncoder.encode(aa.getMachine_image_id(), "UTF-8")
				+ "&instance_size=m1.small&instance_name="
				+ URLEncoder.encode(name, "UTF-8")
				+ "&application_id="
				+ URLEncoder.encode(aa.getId(), "UTF-8")
				+ "&callback_resource_url="
				+ URLEncoder.encode(callback_url, "UTF-8");
			Log.v(TAG, "POST: Params=" + params);
			String JSON = atmo_POST_to_JSON(urlex, params);
			JSONObject orig = (JSONObject) new JSONTokener(JSON).nextValue();
			Log.v(TAG, "POST:" + urlex + " END.");
			JSONObject object = orig.getJSONObject("result");
			String instance_ID = object.getString("value");
			if (instance_ID != null)
				return instance_ID;
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	/********************** KEYPAIRS ******************************/

	private void readKeyPairs() {
		// Get request: No parameters
		try {
			String msg = atmo_GET_to_JSON("getKeyPairsList");
			// Parse JSON
			JSONObject orig = (JSONObject) new JSONTokener(msg).nextValue();
			// Result Object
			JSONObject object = orig.getJSONObject("result");
			// Values (Where images are stored)
			JSONArray value = object.getJSONArray("value");
			JSONObject temp;
			/* Iterate keypairs, save */
			for (int i = 0; i < value.length(); i++) {
				temp = value.getJSONObject(i);
				keypairs.put(temp.getString("keypair_name"),
						temp.getString("keypair_fingerprint"));
			}
		} catch (Exception e) {
			;
		}
	}

	public String getKeyValue(String key) {
		if (keypairs == null)
			getKeyPairs();
		return keypairs.get(key);
	}

	public void creteKeyPair(String keypair_name) {
		// Post request: Parameters keypair_name
		/* Does keypair exist? */
	}

	public void removeKeyPair(String keypair_name) {
		// Post request: Parameters keypair_name
	}

	/********************** IMAGES ******************************/

	private void readImages() {
		try {
			String msg = atmo_GET_to_JSON("getImageList");
			JSONObject response = (JSONObject) new JSONTokener(msg).nextValue();
			JSONObject result = response.getJSONObject("result");
			JSONArray value = result.getJSONArray("value");
			JSONObject temp;
			AtmoImage ai;
			String base, test;
			int x = 1;
			for (int i = 0; i < value.length(); i++) {
				temp = value.getJSONObject(i);
				base = temp.getString("image_name");
				if (base.equals(""))
					base = "Unnamed Image";
				test = base;
				while (images.get(test) != null) {
					test = base + " (" + ++x + ")";
				}
				ai = new AtmoImage(
						test,
						temp.getString("image_description"),
						temp.getString("image_tags"),
						temp.getString("image_id"),
						temp.getString("image_location"),
						temp.getString("image_ownerid"),
						temp.getString("image_state"),
						temp.getString("image_architecture"),
						temp.getString("image_type"),
						temp.getString("image_ramdisk_id"),
						temp.getString("image_kernel_id"),
						(temp.getString("image_is_public").equals("public")) ? true
								: false);
				images.put(test, ai);
			}
		} catch (Exception e) {
			;
		}
	}

	/********************** INSTANCES ******************************/
	public HashMap<String, AtmoInstance> readInstances() {
		// Get request: No parameters
		try {
			String msg = atmo_GET_to_JSON("getInstanceList");
			// Parse JSON
			JSONObject orig = (JSONObject) new JSONTokener(msg).nextValue();
			// Result Object
			JSONObject object = orig.getJSONObject("result");
			// Values (Where images are stored)
			JSONArray value = object.getJSONArray("value");
			JSONObject temp;
			AtmoInstance ai;
			String test, base;
			int prefix;
			/* Iterate keypairs, save */
			for (int i = 0; i < value.length(); i++) {
				temp = value.getJSONObject(i);
				test = base = temp.getString("instance_name");
				prefix = 2;
				while (instances.get(test) != null) {
					test = base + " (" + prefix++ + ")";
				}
				ai = new AtmoInstance(temp.getString("instance_state"),
						temp.getString("instance_description"),
						temp.getString("instance_tags"),
						temp.getString("instance_ami_launch_index"),
						temp.getString("instance_placement"),
						temp.getString("instance_product_codes"),
						temp.getString("group_id"),
						temp.getString("reservation_owner_id"),
						temp.getString("reservation_id"),
						temp.getString("instance_private_dns_name"), test,
						temp.getString("instance_launch_time"),
						temp.getString("instance_key_name"),
						temp.getString("instance_kernel"),
						temp.getString("instance_ramdisk"),
						temp.getString("instance_image_id"),
						temp.getString("instance_num"),
						temp.getString("instance_image_name"),
						temp.getString("instance_public_dns_name"),
						temp.getString("instance_id"),
						temp.getString("instance_instance_type"));

				instances.put(test, ai);

			}
		} catch (Exception e) {
			;
		}
		return instances;
	}

	public String launchInstance(String inst_name, AtmoImage image,
			String keypair) {
		String instance_id = null;
		String urlex = "launchInstance";
		String callback_url = "http://quentin.iplantcollaborative.org/c2dm/C2DM.php";
		try {
			/* URL Encode variables */
			keypair = (keypair == null) ? "" : URLEncoder.encode(keypair,
					"UTF-8");
			String params = ("instance_name="
					+ URLEncoder.encode(inst_name, "UTF-8") + "&instance_size="
					+ URLEncoder.encode("m1.small", "UTF-8") + "&image_id="
					+ URLEncoder.encode(image.getId(), "UTF-8") + "&auth_key="
					+ keypair + "&num_of_instances=" + "1"
					+ "&instance_description="
					+ URLEncoder.encode("Launched via AtmoDroid", "UTF-8")
					+ "&instance_tags=" + URLEncoder.encode("Droid", "UTF-8"))
					+ "&callback_resource_url=" + URLEncoder.encode(callback_url, "UTF-8");
			Log.v(TAG, "POST: Params=" + params);
			String JSON = atmo_POST_to_JSON(urlex, params);
			Log.v(TAG, "POST:" + urlex + " END.");
			JSONObject orig = (JSONObject) new JSONTokener(JSON).nextValue();
			// Result Object
			JSONObject object = orig.getJSONObject("result");
			// Values (Where images are stored)
			String status = object.getString("code");
			if (status.equals("success"))
				instance_id = object.getString("value");

			return instance_id;
		} catch (Exception e) {
			return null;
		}
	}

	public boolean terminateInstance(String instance_id) {
		// Post request: Parameters instance_id
		String urlex = "terminateInstance";
		String params = "instance_id=" + instance_id;
		String JSON = null;
		try {
			Log.v(TAG, "POST: Params=" + params);
			JSON = atmo_POST_to_JSON(urlex, params);
			Log.v(TAG, "POST:" + urlex + " END.");
			JSONObject orig = (JSONObject) new JSONTokener(JSON).nextValue();
			JSONObject object = orig.getJSONObject("result");
			String status = object.getString("code");
			if (status.equals("success"))
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}

	/********************** VOLUMES ******************************/

	public boolean deleteVolume(String volume_id) {
		String params;
		try {
			params = ("volume_id=" + URLEncoder.encode(volume_id, "UTF-8"));
		} catch (Exception e) {
			params = "";
		}

		String urlex = "deleteVolume";
		String JSON = null;
		try {
			JSON = atmo_POST_to_JSON(urlex, params);
			/* END POST REQUEST */
			JSONObject orig = (JSONObject) new JSONTokener(JSON).nextValue();
			// Result Object
			JSONObject object = orig.getJSONObject("result");
			String status = object.getString("code");
			return status.equals("success");
		} catch (Exception e) {
			if (JSON != null)
				return JSON.contains("success");
			return false;
		}

	}

	public String createVolume() {
		String size = "1";
		String name = "atmoCLVolume";
		String desc = "Test_Volume_Created_using_Atmo_CL";
		String tags = "atmoCL,test";
		String params;
		try {
			params = ("size=" + URLEncoder.encode(size, "UTF-8") + "&name="
					+ URLEncoder.encode(name, "UTF-8") + "&description="
					+ URLEncoder.encode(desc, "UTF-8") + "&tags=" + URLEncoder
					.encode(tags, "UTF-8"));
		} catch (Exception e) {
			params = "";
		}

		String urlex = "createVolume";
		/* END POST REQUEST */
		try {
			String JSON = atmo_POST_to_JSON(urlex, params);
			JSONObject orig = (JSONObject) new JSONTokener(JSON).nextValue();
			// Result Object
			JSONObject object = orig.getJSONObject("result");
			// Values (Where images are stored)
			JSONArray value = object.getJSONArray("value");
			String volume = value.getString(0);
			return volume;
		} catch (Exception e) {
			return null;
		}

	}

	public HashMap<String, AtmoVolume> readVolumes() {
		// Get request: No parameters
		try {
			String msg = atmo_GET_to_JSON("getVolumeList");
			// Parse JSON
			JSONObject orig = (JSONObject) new JSONTokener(msg).nextValue();
			// Result Object
			JSONObject object = orig.getJSONObject("result");
			// Values (Where images are stored)
			JSONArray value = object.getJSONArray("value");
			JSONObject temp;
			AtmoVolume av;
			for (int i = 0; i < value.length(); i++) {
				temp = value.getJSONObject(i);
				av = new AtmoVolume(temp.getString("id"),
						temp.getString("tags"),
						temp.getString("attach_data_device"),
						temp.getString("status"),
						temp.getString("description"), temp.getString("name"),
						temp.getString("create_time"),
						temp.getString("attach_data_attach_time"),
						temp.getString("snapshot_id"),
						temp.getString("attach_data_instance_id"),
						temp.getInt("no"), temp.getInt("size"));
				volumes.put(temp.getString("id"), av);
			}
		} catch (Exception e) {
			;
		}
		return volumes;
	}

	public boolean detachVolume(String volume_id) {
		String params;
		try {
			params = ("device=" + URLEncoder.encode("sdb", "UTF-8")
					+ "&instance_id=" + URLEncoder.encode("undefined", "UTF-8")
					+ "&volume_id=" + URLEncoder.encode(volume_id, "UTF-8"));
			String JSON = atmo_POST_to_JSON("detachVolume", params);
			JSONObject orig = (JSONObject) new JSONTokener(JSON).nextValue();
			// Result Object
			JSONObject object = orig.getJSONObject("result");
			// Values (Where images are stored)
			String status = object.getString("code");
			if (status == null || status.length() == 0
					|| status.equals("success") != true)
				return false;
			/*
			 * status = object.getString("value"); if (status == null ||
			 * status.length() == 0 || status.equals("attached") != true) return
			 * false;
			 */
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public boolean attachVolume(String instance_id, String volume_id,
			String device) {
		String params;
		try {
			params = ("device=" + URLEncoder.encode(device, "UTF-8")
					+ "&instance_id=" + URLEncoder.encode(instance_id, "UTF-8")
					+ "&volume_id=" + URLEncoder.encode(volume_id, "UTF-8"));

			if (DEBUG)
				System.out.println(params);

			String JSON = atmo_POST_to_JSON("attachVolume", params);
			if (JSON == null) {
				return false;
			}
			JSONObject orig = (JSONObject) new JSONTokener(JSON).nextValue();
			// Result Object
			JSONObject object = orig.getJSONObject("result");
			// Values (Where images are stored)
			String status = object.getString("code");
			if (status == null || status.length() == 0
					|| status.equals("success") != true)
				return false;
			status = object.getString("value");
			if (status == null || status.length() == 0
					|| status.equals("attaching") != true)
				return false;
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public boolean attachVolume(String instance_id, String volume_id) {
		return attachVolume(instance_id, volume_id, "sdb");
	}

	public boolean askAgain(String dir) {
		System.out.println("Detached volume. Remove the directory <" + dir
				+ ">? (YES/NO)");
		Scanner kb = new Scanner(System.in);
		String answer = kb.next();
		return (answer.equals("YES"));
	}

	public static void logCommand(String command, String password) {
		if (password != "")
			command = command.replace(password, "<password>");
		System.out.println("CMD:" + command);
	}

	/**
	 * Steps to unmounting a volume: 1. Unmount from unmountdir 2. Detach volume
	 * 3. Remove 'unmountdir' directory
	 * 
	 * @param volume
	 * @param unmountdir
	 * @param password
	 */
	public void unmountVolume(String volume, String unmountdir, String password) {
		String sudoBegin = "echo \"" + escape(password) + "\" | sudo -S ";
		String command = sudoBegin + "umount " + unmountdir;
		if (DEBUG) {
			logCommand(command, password);
		}
		int returned = SystemCall.returnCodePipe(command);
		if (returned == 0) {
			System.out.println("Un-Mounted volume from " + unmountdir);
			System.out
					.println("Sending detach request. Waiting for reply..(10seconds)");
			waitFor(10);
			if (!detachVolume(volume)) {
				System.out.println("Failed to detach volume. Retry command.");
				return;
			}
			System.out.println("Detached volume " + volume + ".");

			if (askAgain(unmountdir)) {
				command = "echo \"" + AtmoAPI.escape(password)
						+ "\" | sudo -S " + "rm -rf " + unmountdir;
				if (DEBUG) {
					logCommand(command, password);
				}
				returned = SystemCall.returnCodePipe(command);
				// if(returned == 0 && DEBUG) {
				// System.out.println("Removed directory");
				// }
			} else {
				System.out.println("Detached volume " + volume + ".");
			}
		} else {
			System.out.println("Error unmounting volume");
		}
	}

	public boolean makeDir(String password, String mountto) {
		File f = new File(mountto);
		if (!f.exists()) {
			String command = "echo \"" + escape(password) + "\" | sudo -S ";
			command += "mkdir -p " + mountto;
			if (DEBUG) {
				logCommand(command, password);
			}
			SystemCall.runPipeCommand(command);
		} else {
			return true;
		}
		waitFor(3);
		if (SystemCall.returnCode("find " + mountto) != 0) {
			return false;
		} else {
			return true;
		}
	}

	public boolean mountDrive(String password, String fromdir, String mountto) {
		String command = "echo \"" + escape(password) + "\" | sudo -S "
				+ "/bin/mount -t ext3 " + fromdir + " " + mountto;
		if (DEBUG) {
			System.out.println("CMD3:"
					+ command.replace(password, "<password>"));
		}
		BufferedReader br = SystemCall.runPipeCommand(command);
		String read;
		try {
			while ((read = br.readLine()) != null) {
				if (DEBUG) {
					System.out.println(read);
				}
				if (read.contains("error")) {
					System.out
							.println("Volume is formatted, but failed to mount to "
									+ fromdir);
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public String checkFormat(String password, String fromdir) {
		String read;
		// String command = "/sbin/tune2fs -l " + fromdir;
		String command = "echo \"" + escape(password) + "\" | sudo -S "
				+ "/sbin/tune2fs -l " + fromdir;
		if (DEBUG) {
			System.out.println("CMD2:"
					+ command.replace(password, "<password>"));
		}
		BufferedReader br = SystemCall.runPipeCommand_err(command);

		int length = 0;
		try {
			while ((read = br.readLine()) != null) {
				if (DEBUG) {
					System.out.println(read);
				}
				if (read.contains("Bad magic number")) {
					return "format";
				}
				if (read.contains("find valid filesystem")) {
					return "missing";
				}
				length++;
			}
			// if (length == 1) {
			// return "empty";
			// }
			return "formatted";
		} catch (Exception e) {
			return "error";
		}
	}

	public boolean formatDrive(String password, String fromdir) {

		String command = "echo \"" + escape(password) + "\" | sudo -S "
				+ "/sbin/mkfs.ext3 -F " + fromdir;
		if (DEBUG) {
			logCommand(command, password);
		}
		int rc = SystemCall.returnCodePipe(command);
		if (rc == 0) {
			if (DEBUG)
				System.out.println("Formatted volume.");
			return true;
		}
		return false;
	}

	public boolean changePermissions(String password, String directory) {
		String user = null;
		String group = null;
		BufferedReader br;
		try {
			br = SystemCall.runCommand("whoami");
			String read = br.readLine();
			if (read != null)
				user = read;

			br = SystemCall.runCommand("id -g -n " + user);
			read = br.readLine();
			if (read != null)
				group = read;
		} catch (Exception e) {
			;
		}

		if (user == null || group == null) {
			System.out.println("Error retrieving username & group");
			return false;
		}
		String command = "echo \"" + escape(password) + "\" | sudo -S "
				+ "chown -R " + user + ":" + group + " " + directory;
		if (DEBUG) {
			System.out.println("CMD4:"
					+ command.replace(password, "<password>"));
		}
		int retcode = SystemCall.returnCodePipe(command);
		if (DEBUG) {
			System.out.println("Changed owner to user:" + user);
		}
		if (retcode != 0) {
			System.out.println("Failed to change permissions on dir:"
					+ directory);
			return false;
		}
		return true;
	}

	public boolean askFormat(String password, String formatme) {
		System.out.println("No formatted driver found at " + formatme);
		Scanner kb = new Scanner(System.in);
		System.out.print("Format " + formatme + "? (YES/NO):");
		String ans = kb.nextLine();
		if (ans.equals("YES")) {
			System.out.println("Formatting drive. This can take a while..");
			return (formatDrive(password, formatme));
		} else {
			System.out
					.println("User cancelled formatting of driver. Operation aborted.");
			return false;
		}
	}

	public void mountVolume(String mountfrom, String mountto, String password) {
		String formtest;
		boolean choice = false;

		// Test 1: Does this directory exist? If not, make it.
		if (!makeDir(password, mountto)) {
			System.out.println("Failed to create directory :" + mountto + ".");
			return;
		}

		// Has user specifically selected the device to use? (like
		// /dev/[sdc,sdd,...]
		if (!mountfrom.equals("/dev/sdb")) {
			System.out.println("Using the driver:" + mountfrom);
			formtest = checkFormat(password, mountfrom);
			if (formtest.equals("format"))
				if (!askFormat(password, mountfrom)) {
					System.out.println("Format failed.");
					return;
				}
			choice = true;
		}
		// Search for a formatted device
		if (!choice)
			for (String s : new String[] { "", "1", "2", "3", "4" }) {
				s = mountfrom + s;
				if (DEBUG) {
					System.out.println("Looking for formatted device:" + s);
				}
				formtest = checkFormat(password, s);
				if (formtest.equals("formatted")) {
					System.out.println("Using the driver found at " + s);
					mountfrom = s;
					choice = true;
					break;
				}
			}
		// No formatted device, offer to format entire device.
		if (!choice) {
			if (askFormat(password, mountfrom)) {
				choice = true;
			} else {
				System.out.println("Format failed.");
				return;
			}
		}
		// Error
		if (!choice) {
			System.out.println("No drivers found on " + mountfrom);
			return;
		}

		// ASSERT: mountfrom contains a formatted driver
		System.out.println("Mounting drive. This can take some time..");
		if (!mountDrive(password, mountfrom, mountto)) {
			System.out.println("Mount failed.");
			return;
		}

		if (!changePermissions(password, mountto)) {
			System.out.println("Error changing permissions on " + mountto);
			return;
		}

		System.out.println("Volume mounted to " + mountto + ".");
		return;
	}

	/************************ UTILITIES ****************************/

	public void runAtmoNotifier() {
		authIfNotValid();
		// Notification code...
	}

	public void authIfNotValid() {
		if (credentials.get("X-Auth-Token") != null)
			return;
		if (validdate == null || validdate.compareTo(new Date()) < 0) {
			authenticate(credentials.get("X-Auth-User"),
					credentials.get("X-Auth-Key"));
		}
	}

	/**
	 * Authenticates on atmo server, if successful the credentials and
	 * validation time are set. Validation times are set to expire 24 hours
	 * after assignment.
	 * 
	 * @param username
	 * @param password
	 * @return Status true = Authentication Successful
	 */
	public boolean authenticate(String username, String password) {
		boolean status = false;
		Calendar c = Calendar.getInstance();
		try {
			if (credentials != null) {
				credentials = validate(username, password, null);
				credentials.put("Accept", "text/plain");
				credentials.put("Content-type",
						"application/x-www-form-urlencoded");
				credentials.put("X-Api-Version", "v1");
			}
			String dt = credentials.get("X-Validation-Time");
			SimpleDateFormat from = new SimpleDateFormat(
					"EEE, dd MMM yyyy kk:mm:ss z");
			SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd hh:mma");
			c.setTime(from.parse(dt));
			c.add(Calendar.DATE, 1);
			dt = to.format(c.getTime()); // Useful for debugging time
			status = true;
		} catch (Exception e) {
			;
		} finally {
			validdate = c.getTime();
			keypairs = null;
			images = null;
			apps = null;
			instances = null;
		}
		return status;
	}

	/**
	 * Used by Authenticate to contact server if user/pass combination is
	 * successful, the response is returned as a HashMap.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	private HashMap<String, String> validate(String username, String password,
			HttpsURLConnection conn) {
		HashMap<String, String> retlist = new HashMap<String, String>();
		try {
			retlist.put("X-Auth-User", username);
			retlist.put("X-Auth-Key", password);

			System.setProperty("http.keepAlive", "false");
			URL url = new URL(myserver);
			conn = (HttpsURLConnection) url.openConnection();

			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("Accept", "text/plain");
			conn.setRequestProperty("X-Auth-User", retlist.get("X-Auth-User"));
			conn.setRequestProperty("X-Auth-Key", retlist.get("X-Auth-Key"));
			conn.setRequestProperty("User-Agent", "AtmoDroid/Test");

			int rc = conn.getResponseCode();
			if (rc != 200) {
				return retlist;
			}

			Map<String, List<String>> map = conn.getHeaderFields();

			List<String> token = (map.get("X-Auth-Token") != null) ? map
					.get("X-Auth-Token") : map.get("x-auth-token");

			retlist.put("X-Auth-Token", token.get(0));

			token = (map.get("X-Server-Management-Url") != null) ? map
					.get("X-Server-Management-Url") : map
					.get("x-server-management-url");
			retlist.put("X-Api-Server", token.get(0));

			token = (map.get("Date") != null) ? map.get("Date") : map
					.get("date");
			retlist.put("X-Validation-Time", token.get(0));

		} catch (Exception e) {
			Log.e("ATMODROID", e.getMessage());
		} finally {
			conn.disconnect();
		}
		return retlist;
	}

	private boolean register_device(String params) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL("http://quentin.iplantcollaborative.org/c2dm/C2DM.php");
			System.setProperty("http.keepAlive", "false");
			conn = (HttpURLConnection) url.openConnection();
			/* Set header correctly */
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("Accept", "text/plain");
			conn.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(
					conn.getOutputStream());

			writer.write(params);
			writer.flush();

			int rc = conn.getResponseCode();
			if (rc != 200) {
				if (MOBILE) {
					Log.e(TAG, "Error: return code -" + rc);
				}
			}
			writer.close();
			return true;
		} catch (Exception e) {
			Log.e(TAG,"Error registering Device:",e);
		}
		return false;
	}

	private String atmo_POST_to_JSON(String urlex, String params) {
		/* BEGIN POST REQUEST */
		Log.v(TAG, "POST:" + urlex);
		String JSON = null;
		HttpsURLConnection conn = null;
		try {
			URL url = new URL(this.getServer() + "/" + urlex);
			System.setProperty("http.keepAlive", "false");
			conn = (HttpsURLConnection) url.openConnection();
			/* Set header correctly */
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("Accept", "text/plain");
			conn.setRequestProperty("X-Auth-User", this.getUser());
			conn.setRequestProperty("X-Auth-Token", this.getToken());
			conn.setRequestProperty("X-Api-Server", this.getServer());
			conn.setRequestProperty("X-Api-Version", this.getVersion());

			conn.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(
					conn.getOutputStream());

			writer.write(params);
			writer.flush();

			int rc = conn.getResponseCode();
			if (rc != 200) {
				if (DEBUG) {
					System.out.println("Invalid Response. RC=" + rc);
				}
				if (MOBILE) {
					Log.e(TAG, "Error: return code -" + rc);
				}
			}

			String line = "";
			String msg = "";
			InputStream is = conn.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			while ((line = br.readLine()) != null) {
				msg += line;
			}
			JSON = msg;
			writer.close();
			br.close();
		} catch (Exception e) {
			Log.e(TAG, "---");
			Log.e(TAG, "Error:" + e.getMessage());
			Log.e(TAG, "Error:" + Log.getStackTraceString(e));
			if (JSON != null)
				Log.e(TAG, "Error:" + JSON);
			Log.e(TAG, "---");
		} finally {
			Log.v(TAG, "POST:" + JSON);
			if (conn != null)
				conn.disconnect();
		}
		return JSON;
	}

	private String atmo_GET_to_JSON(String urlex) {
		Log.v(TAG, "GET :" + urlex);
		String msg = null;
		HttpsURLConnection conn = null;
		try {
			URL url = new URL(this.getServer() + "/" + urlex);
			System.setProperty("http.keepAlive", "false");
			conn = (HttpsURLConnection) url.openConnection();

			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("Accept", "text/plain");
			conn.setRequestProperty("X-Auth-User", this.getUser());
			conn.setRequestProperty("X-Auth-Token", this.getToken());
			conn.setRequestProperty("X-Api-Server", this.getServer());
			conn.setRequestProperty("X-Api-Version", this.getVersion());

			int rc = conn.getResponseCode();
			if (rc != 200) {
				if (DEBUG) {
					System.out.println("Invalid Response. RC=" + rc);
				}
				if (MOBILE) {
					Log.e(TAG, "Error: return code -" + rc);
				}
			}

			String line = "";
			msg = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			while ((line = br.readLine()) != null) {
				msg += line;
			}
			br.close();
		} catch (Exception e) {
			Log.e(TAG, "---");
			Log.e(TAG, "Error:" + e.getMessage());
			Log.e(TAG, Log.getStackTraceString(e));
			if (msg != null)
				Log.e(TAG, "JSON:" + msg);
			Log.e(TAG, "---");
		} finally {
			if (conn != null)
				conn.disconnect();
			Log.v(TAG, "GET :" + msg);
		}
		return msg;
	}

	public void waitFor(int i) {
		try {
			Thread.currentThread();
			Thread.sleep(i * 1000);
		} catch (Exception e) {
			;
		}
	}

	public static String escape(String password) {
		// Cause 'new shell' commands (for piped input) to crash
		password = password.replace("$", "\\$");
		password = password.replace("&", "\\&");
		password = password.replace("*", "\\*");
		password = password.replace(".", "\\.");
		password = password.replace(",", "\\,");
		password = password.replace("?", "\\?");
		password = password.replace("<", "\\<");
		password = password.replace(">", "\\>");
		password = password.replace("|", "\\|");
		password = password.replace("!", "\\!");
		password = password.replace("{", "\\{");
		password = password.replace("}", "\\}");
		password = password.replace("%", "\\%");
		password = password.replace("[", "\\[");
		password = password.replace("]", "\\]");
		// Cause password to crash
		password = password.replace("`", "\\`");
		password = password.replace("\"", "\\\"");
		password = password.replace("\'", "\\\'");
		password = password.replace("\\", "\\\\");
		return password;
	}

	// PARCELABLE INTEGRATION
	@SuppressWarnings("unchecked")
	private AtmoAPI(Parcel in) {
		apps = (HashMap<String, AtmoApp>) in.readSerializable();
		credentials = (HashMap<String, String>) in.readSerializable();
		images = (HashMap<String, AtmoImage>) in.readSerializable();
		instances = (HashMap<String, AtmoInstance>) in.readSerializable();
		keypairs = (HashMap<String, String>) in.readSerializable();
		validdate = (Date) in.readSerializable();
		volumes = (HashMap<String, AtmoVolume>) in.readSerializable();
		myserver = in.readString();
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(apps);
		dest.writeSerializable(credentials);
		dest.writeSerializable(images);
		dest.writeSerializable(instances);
		dest.writeSerializable(keypairs);
		dest.writeSerializable(validdate);
		dest.writeSerializable(volumes);
		dest.writeString(myserver);
	}

	public static final Parcelable.Creator<AtmoAPI> CREATOR = new Parcelable.Creator<AtmoAPI>() {
		public AtmoAPI createFromParcel(Parcel in) {
			return new AtmoAPI(in);
		}

		public AtmoAPI[] newArray(int size) {
			return new AtmoAPI[size];
		}
	};

	public int describeContents() {
		return 0;
	}

	// END PARCELABLE INTEGRATION

	// C2DM Integration
	public void setRegistrationID(String registrationID) {
		this.registrationID = registrationID;
		String params = "user=" + getUser() + "&registrationID="
				+ registrationID;
		register_device(params);
	}

	public String getRegistrationID() {
		return registrationID;
	}
	// END C2DM Integration
}