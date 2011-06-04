package fr.unice.aap.musics;

import java.io.File;
import java.io.FileFilter;

public class AudioFileFilter implements FileFilter {
	private enum SupportedFileTypes {
		GPP(),
		MPEG4(),
		MP3(),
		MIDI(),
		RTTTL(),
		RTX(),
		OTA(),
		IMelody(),
		Ogg(),
		WAVE();
		
		public static SupportedFileTypes getSupportedFileType(String p_ext) {
			if(p_ext == null || p_ext.isEmpty())
				return null;
			
			if(p_ext.equalsIgnoreCase(".3gp"))
				return GPP;
			if(p_ext.equalsIgnoreCase(".mp4") || p_ext.equalsIgnoreCase(".m4a"))
				return MPEG4;
			if(p_ext.equalsIgnoreCase(".mp3"))
				return MP3;
			if(p_ext.equalsIgnoreCase(".mid") || p_ext.equalsIgnoreCase(".xmf") || p_ext.equalsIgnoreCase(".mxmf"))
				return MIDI;
			if(p_ext.equalsIgnoreCase(".rtttl"))
				return RTTTL;
			if(p_ext.equalsIgnoreCase(".rtx"))
				return RTX;
			if(p_ext.equalsIgnoreCase(".ota"))
				return OTA;
			if(p_ext.equalsIgnoreCase(".imy"))
				return IMelody;
			if(p_ext.equalsIgnoreCase(".ogg"))
				return Ogg;
			if(p_ext.equalsIgnoreCase(".wav"))
				return WAVE;
			
			return null;
		}
	}
	
	@Override
	public boolean accept(File pathname) {
		if(pathname == null || pathname.getName() == null || pathname.getName().isEmpty())
			return false;
		
		if(!pathname.getName().contains("."))
			return false;
		
		String ext = pathname.getName().substring(pathname.getName().lastIndexOf("."), pathname.getName().length());

		return (SupportedFileTypes.getSupportedFileType(ext) != null);
	}
}
