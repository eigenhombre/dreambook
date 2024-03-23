# dreambook

![build](https://github.com/eigenhombre/dreambook/actions/workflows/build.yml/badge.svg)

`dreambook` converts a dream journal into an ebook (EPUB format).

## Requirements

- [Babashka](https://github.com/babashka/babashka), a fast-starting Clojure interpreter for scripts.
- A dream journal, written in [Org Mode format](https://orgmode.org/), similar in format to
  the one in `example/dreams.org`.

Tested on Mac.  Probably works on Linux.

Resulting EPUB book tested in Calibre, on Apple Books (Mac, iPad,
iPhone), and occasionally on a Kindle and a Kobo device.

## Format

See `example/dreams.org` for an example of the format.  Dreams must be
entered in subsections using Org's date format; the subsections for each
year should fall under a top-level heading for that year.  For example:

```
* 2011

** <2011-04-26 Tue>

After a long day of work, I am walking to the train.  Without warning, [....]
```

## Usage

There is no installation per se; just clone the repository and run the script.

    ./dreambook

Then, open `dreams.epub` in your favorite ebook reader.

The default input file is `$HOME/org/dreams.org`; an optional introduction
file `$HOME/org/dreams-intro.org` can be used to set up frontmatter to the book
and `$HOME/org/dreams-collophon.org` can be used to set up any concluding content.
These file locations are configurable:

    ./dreambook -d /path/to/dreams.org \
                -i /path/to/dreams-intro.org \
                -c /path/to/dreams-collophon.org \
                -j /path/to/dreams-cover.png

See also `docker-smoketest` for a working example used in the CI build.

### Options

```
  -d, --dreamsfile    $HOME/org/dreams.org
  -i, --introfile     $HOME/org/dreams-intro.org
  -c, --collophonfile $HOME/org/dreams-collophon.org
  -j, --coverfile     $HOME/org/dreams-cover.png
  -a, --author        $USER
  -t, --title         eBook of Dreams
  -w, --words
  -h, --help
```

## Example Output

In addition to creating the EPUB file, `dreambook` prints a report of number of dreams
per year, as well as the total number of dreams:

```
$ ./dreambook

EPUB 'dreams.epub' generated successfully.
1985:   68 ....................................................................
1986:   59 ...........................................................
1987:   10 ..........
1988:    7 .......
1989:    9 .........
1990:   29 .............................
1998:    1 .
2000:   10 ..........
2001:    2 ..
2002:   34 ..................................
2003:    3 ...
2004:    3 ...
2005:    4 ....
2006:    1 .
2011:    4 ....
2017:    2 ..
2019:   68 ....................................................................
2021:    2 ..
2022:   20 ....................
2023:   14 ..............
2024:   35 ...................................
TOTAL: 385
$
```

Or, on the supplied example,

```
$ ./dreambook -d example/dreams.org \
              -i example/intro.org \
              -c example/collophon.org \
              -j example/cover.png

EPUB 'dreams.epub' generated successfully.
1987:    1 .
2003:    1 .
TOTAL: 2
$
```

## Word Frequencies

It can be interesting to see what words are most common in your
dreams.  To view the top words (excluding the most common English
words), run:

    ./dreambook -w   # (or --words)

Example:

```
$ ./dreambook --words -d example/dreams.org
are basement bus crawl eating feel fifteen find fit
friends house i'm it's kfc manhole many place quiet sitting
$
```

## Other Actions

- `bb test` runs the tests
- `bb fmt` reformats the code using [`cljf`](https://github.com/candid82/cljf) (install separately)

# LICENSE

Copyright © 2024, John Jacobsen. MIT License.

## Disclaimer

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
