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
