# INSTALL

## Syntax highlighting support for Adams spectrum files

* place `ftplugin/adamsflow.vim` in `$HOME/.vim/ftplugin`
* place `syntax/adamsflow.vim` in `$HOME/.vim/syntax`
* edit `$HOME/.vim/filetype.vim` that it looks similar to this:

```
  if exists("did_load_filetypes") 
    finish
  endif

  augroup filetypedetect
    " Adams spectrum files
    au! BufRead,BufNewFile *.spec     setfiletype adamsspectra
  augroup END
```
