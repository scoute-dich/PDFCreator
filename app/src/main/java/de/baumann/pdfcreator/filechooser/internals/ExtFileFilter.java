package de.baumann.pdfcreator.filechooser.internals;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by coco on 6/7/15.
 * Part of "android-file-chooser"
 * modified by Gaukler Faun
 */

public class ExtFileFilter implements FileFilter {
    private final boolean m_allowHidden;
    private final boolean m_onlyDirectory;
    private final String[] m_ext;

    @SuppressWarnings("unused")
    public ExtFileFilter() {
        this(false, false);
    }

    @SuppressWarnings("unused")
    public ExtFileFilter(String... ext_list) {
        this(false, false, ext_list);
    }

    public ExtFileFilter(boolean dirOnly, boolean hidden, String... ext_list) {
        m_allowHidden = hidden;
        m_onlyDirectory = dirOnly;
        m_ext = ext_list;
    }

    @Override
    public boolean accept(File pathname) {
        if (!m_allowHidden) {
            if (pathname.isHidden())
                return false;
        }

        if (m_onlyDirectory) {
            if (!pathname.isDirectory())
                return false;
        }

        if (m_ext == null)
            return true;

        if (pathname.isDirectory())
            return true;

        String ext = FileUtil.getExtension(pathname).substring(1);
        for (String e : m_ext) {
            if (ext.equalsIgnoreCase(e))
                return true;
        }
        return false;
    }

}
