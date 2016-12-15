package de.baumann.pdfcreator.filechooser.internals;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Created by coco on 6/7/15.
 * Part of "android-file-chooser"
 * modified by Gaukler Faun
 */

public class RegexFileFilter implements FileFilter {
    private final boolean m_allowHidden;
    private final boolean m_onlyDirectory;
    private final Pattern m_pattern;

    @SuppressWarnings("unused")
    public RegexFileFilter() {
        this(null);
    }

    private RegexFileFilter(@SuppressWarnings({"SameParameterValue", "UnusedParameters"}) Pattern ptn) {
        //noinspection RedundantCast
        this(false, (Pattern) null);
    }

    @SuppressWarnings("unused")
    public RegexFileFilter(boolean dirOnly, boolean hidden, String ptn) {
        m_allowHidden = hidden;
        m_onlyDirectory = dirOnly;
        m_pattern = Pattern.compile(ptn, Pattern.CASE_INSENSITIVE);
    }

    public RegexFileFilter(boolean dirOnly, boolean hidden, String ptn, int flags) {
        m_allowHidden = hidden;
        m_onlyDirectory = dirOnly;
        m_pattern = Pattern.compile(ptn, flags);
    }

    private RegexFileFilter(@SuppressWarnings({"SameParameterValue", "UnusedParameters"}) boolean dirOnly, Pattern ptn) {
        m_allowHidden = false;
        m_onlyDirectory = false;
        m_pattern = ptn;
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

        if (m_pattern == null)
            return true;

        if (pathname.isDirectory())
            return true;

        String name = pathname.getName();
        return m_pattern.matcher(name).matches();
    }

}
