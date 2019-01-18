package com.pisen.router.core.filemanager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.InputStreamEntity;

import android.studio.util.URLUtils;

import com.pisen.router.core.filemanager.ResourceInfo.RSource;

import de.aflx.sardine.DavResource;
import de.aflx.sardine.Sardine;
import de.aflx.sardine.SardineFactory;

/**
 * 路由文件操作
 */

public abstract class SardineResourceManager extends ResourceManager {

	protected Sardine sardine;

	public SardineResourceManager() {
		this(null, null);
	}

	public SardineResourceManager(String username, String password) {
		sardine = SardineFactory.begin(username, password);
	}

	@Override
	protected List<ResourceInfo> listFileChooser(String dir, boolean dirOnly) {
		List<ResourceInfo> results = new ArrayList<ResourceInfo>();
		try {
			List<DavResource> files = sardine.list(encodeURL(dir));
			files.remove(0);
			for (DavResource res : files) {
				if (dirOnly) {
					if (res.isDirectory()) {
						ResourceInfo info = toResourceInfo(dir, res);
						results.add(info);
					}
				} else {
					ResourceInfo info = toResourceInfo(dir, res);
					results.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return results;
	}

	public static ResourceInfo toResourceInfo(String dir, DavResource res) {
		ResourceInfo info = new ResourceInfo(RSource.Remote);
		info.isDirectory = res.isDirectory();
		info.path = dir + res.getName() + (res.isDirectory() ? "/" : "");
		info.name = res.getName();
		info.size = res.getContentLength();
		info.lastModified = res.getModified().getTime();
		return info;
	}

	@Override
	public boolean exists(String path) throws Exception {
		return sardine.exists(encodeURL(path));
	}

	@Override
	public void createDir(String path) throws Exception {
		path = pathToDir(path);
		sardine.createDirectory(encodeURL(path));
	}

	@Override
	public InputStream get(String path) throws Exception {
		return sardine.get(encodeURL(path));
	}

	@Override
	public void put(String path, InputStream inStream) throws Exception {
		put(path, new InputStreamEntity(inStream, -1));
	}

	@Override
	public void put(String path, InputStreamEntity inStream) throws Exception {
		sardine.put(encodeURL(path), inStream);
	}

	@Override
	public void copy(String sourcePath, String targetPath) throws Exception {
		sardine.copy(encodeURL(sourcePath), encodeURL(targetPath));
	}

	@Override
	public void move(String sourcePath, String targetPath) throws Exception {
		sardine.move(encodeURL(sourcePath), encodeURL(targetPath));
	}

	@Override
	public void delete(String path) throws Exception {
		sardine.delete(encodeURL(path));
	}

	@Override
	public void rename(String sourcePath, String newName) throws Exception {
		String newURL = getRenameURL(sourcePath, newName);
		sardine.move(encodeURL(sourcePath), encodeURL(newURL));
	}

	private static String getRenameURL(String sourcePath, String newName) {
		String newURL = URLUtils.getParentURI(sourcePath) + newName;
		if (isDirPath(sourcePath)) {
			newURL = newURL + "/";
		}
		return newURL;
	}

	public static String pathToDir(String path) {
		path = path + (isDirPath(path) ? "" : "/");
		return path;
	}

	public static boolean isDirPath(String path) {
		return path.endsWith("/");
	}

	public static String encodeURL(String url) {
		return URLUtils.encodeURL(url);
	}

}
