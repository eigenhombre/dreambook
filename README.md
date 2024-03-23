# dreambook

`dreambook` converts a dream journal into an ebook.

## Requirements

- [Babashka](https://github.com/babashka/babashka)
- A dream journal, written in Org Mode format, similar in format to
  the one in `examples/dreams.org`.

Tested on Mac.  Probably works on Linux.

Resulting EPUB book tested in Calibre, on Apple Books (Mac, iPad,
iPhone), and occasionally on a Kindle and a Kobo device.

## Installation

- Clone this repository
- `cd` into the repository

## Usage

    ./dreambook

Then, open `dreams.epub` in your favorite ebook reader.

The default input file is `$HOME/org/dreams.org`; an optional introduction
file `$HOME/org/dreams-intro.org` can be used to set up frontmatter to the book
and `$HOME/org/dreams-collophon.org` can be used to set up any concluding content.
These file locations are configurable:

    ./dreambook -i /path/to/dreams.org -I /path/to/dreams-intro.org -C /path/to/dreams-collophon.org

## Example Output

In addition to creating the EPUB file, `dreambook` prints a report of number of dreams
per year, as well as the total number of dreams:

```
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
