" Vim syntax file
" Language:	ADAMS spectrum file
" Maintainer:	fracpete
" Last Change:	2025 Aug 28

" Syntax highlighting for ADAMS flow files

" For version 5.x: Clear all syntax items
" For version 6.x: Quit when a syntax file was already loaded
if version < 600
  syntax clear
elseif exists("b:current_syntax")
  finish
endif

syn case ignore

" keywords
syn match specWaveno "waveno"
syn match specAmplitude "amplitude"
syn match specSeparator "---"

" numbers
syn match  specNumber	"-\=\<\d\+\>"
syn match  specFloat	"-\=\<\d\+\.\d\+\>"
syn match  specFloat	"-\=\<\d\+\.\d\+[eE]-\=\d\+\>"

" comments
syn match  specComment	"#.*"

" Define the default highlighting.
" For version 5.7 and earlier: only when not done already
" For version 5.8 and later: only when an item doesn't have highlighting yet
if version >= 508 || !exists("did_flows_syn_inits")
  if version < 508
    let did_spec_syn_inits = 1
    command -nargs=+ HiLink hi link <args>
  else
    command -nargs=+ HiLink hi def link <args>
  endif

  HiLink specComment	Comment
  HiLink specWaveno	Type
  HiLink specAmplitude	Type
  HiLink specSeparator	Type
  HiLink specFloat	Float
  HiLink specNumber	Number

  delcommand HiLink
endif

let b:current_syntax = "adamsspectra"

" vim: ts=8
