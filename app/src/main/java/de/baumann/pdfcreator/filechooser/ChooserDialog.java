package de.baumann.pdfcreator.filechooser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import de.baumann.pdfcreator.R;
import de.baumann.pdfcreator.filechooser.internals.DirAdapter;
import de.baumann.pdfcreator.filechooser.internals.ExtFileFilter;
import de.baumann.pdfcreator.filechooser.internals.RegexFileFilter;

/**
 * Created by coco on 6/7/15.
 * Part of "android-file-chooser"
 * modified by Gaukler Faun
 */
@SuppressWarnings("unused")
public class ChooserDialog implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener {

    public interface Result {
        void onChoosePath(String dir, File dirFile);
    }

    public ChooserDialog() {

    }

    public ChooserDialog with(Context cxt) {
        this._context = cxt;
        return this;
    }

    public ChooserDialog withFilter(FileFilter ff) {
        withFilter(false, false, (String[]) null);
        this._fileFilter = ff;
        return this;
    }

    public ChooserDialog withFilter(boolean dirOnly, boolean allowHidden, FileFilter ff) {
        withFilter(dirOnly, allowHidden, (String[]) null);
        this._fileFilter = ff;
        return this;
    }

    public ChooserDialog withFilter(boolean allowHidden, String... suffixes) {
        return withFilter(false, allowHidden, suffixes);
    }

    public ChooserDialog withFilter(boolean dirOnly, boolean allowHidden, String... suffixes) {
        this._dirOnly = dirOnly;
        if (suffixes == null)
            this._fileFilter = dirOnly ? filterDirectoriesOnly : filterFiles;
        else
            this._fileFilter = new ExtFileFilter(_dirOnly, allowHidden, suffixes);
        return this;
    }

    public ChooserDialog withFilterRegex(boolean dirOnly, boolean allowHidden, String pattern, int flags) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexFileFilter(_dirOnly, allowHidden, pattern, flags);
        return this;
    }

    public ChooserDialog withFilterRegex(boolean dirOnly, boolean allowHidden, String pattern) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexFileFilter(_dirOnly, allowHidden, pattern, Pattern.CASE_INSENSITIVE);
        return this;
    }

    public ChooserDialog withStartFile(String startFile) {
        if (startFile != null)
            _currentDir = new File(startFile);
        else
            _currentDir = Environment.getExternalStorageDirectory();

        if(!_currentDir.isDirectory())
            _currentDir = _currentDir.getParentFile();

        return this;
    }

    public ChooserDialog withChosenListener(Result r) {
        this._result = r;
        return this;
    }

    public ChooserDialog withResources() {
        this._titleRes = R.string.choose_file;
        this._okRes = R.string.title_choose;
        this._cancelRes = R.string.dialog_cancel;
        return this;
    }

    public ChooserDialog build() {
        if (_titleRes == 0 || _okRes == 0 || _cancelRes == 0)
            throw new RuntimeException("withResources() should be called at first.");

        DirAdapter adapter = refreshDirs();

        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        //builder.setTitle(R.string.dlg_choose dir_title);
        builder.setTitle(_titleRes);
        builder.setAdapter(adapter, this);

        if (_dirOnly) {
            builder.setPositiveButton(_okRes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (_result != null) {
                        if (_dirOnly)
                            _result.onChoosePath(_currentDir.getAbsolutePath(), _currentDir);
                    }
                    dialog.dismiss();
                }
            });
        }

        builder.setNegativeButton(_cancelRes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        _alertDialog = builder.create();
        _list = _alertDialog.getListView();
        _list.setOnItemClickListener(this);
        return this;
    }

    public void show() {
        //if (_result == null)
        //    throw new RuntimeException("no chosenListener defined. use withChosenListener() at first.");
        if (_alertDialog == null || _list == null)
            throw new RuntimeException("call build() before show().");
        _alertDialog.show();
    }


    private void listDirs() {
        _entries.clear();

        // Get files
        File[] files = _currentDir.listFiles(_fileFilter);

        // Add the ".." entry
        if (_currentDir.getParent() != null)
            _entries.add(new File(".."));

        if (files != null) {
            Collections.addAll(_entries, files);
        }

        Collections.sort(_entries, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        });
    }

    private void listDirs2() {
        _entries.clear();

        // Get files
        File[] files = _currentDir.listFiles();

        // Add the ".." entry
        if (_currentDir.getParent() != null)
            _entries.add(new File(".."));

        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory())
                    continue;

                _entries.add(file);
            }
        }

        Collections.sort(_entries, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View list, int pos, long id) {
        if (pos < 0 || pos >= _entries.size())
            return;

        File file = _entries.get(pos);
        if (file.getName().equals(".."))
            _currentDir = _currentDir.getParentFile();
        else
            _currentDir = file;

        if (!file.isDirectory()) {
            if (!_dirOnly) {
                if (_result != null) {
                    _result.onChoosePath(file.getAbsolutePath(), file);
                    _alertDialog.dismiss();
                    return;
                }
            }
        }

        refreshDirs();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        //
    }

    private DirAdapter refreshDirs() {
        listDirs();
        DirAdapter adapter = new DirAdapter(_context, _entries);
        if (_list != null)
            _list.setAdapter(adapter);
        return adapter;
    }


    private final List<File> _entries = new ArrayList<>();
    private File _currentDir;
    private Context _context;
    private AlertDialog _alertDialog;
    private ListView _list;
    private Result _result = null;
    private boolean _dirOnly;
    private FileFilter _fileFilter;
    private int _titleRes = R.string.choose_file, _okRes = R.string.title_choose, _cancelRes = R.string.dialog_cancel;

    private static final FileFilter filterDirectoriesOnly = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };

    private static final FileFilter filterFiles = new FileFilter() {
        public boolean accept(File file) {
            return !file.isHidden();
        }
    };


}
