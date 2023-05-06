
Create a simple Calculator REST API using JSON that supports input of mathematical expressions with the basic operations: `+`, `-`, `*`, and `/`. It should allow the usage of parens and understand operator precedence.

Example:
POST /calc `{"expression": "-1 * (2 * 6 / 3)"}`
Returns `{"result": "-4"}`

Extra credits for the ability to retrieve history of calculations done.


## Getting Started
If you already have set-up the development environment:
1. Start the application: `lein run`
2. Go to [localhost:8080](http://localhost:8080/) to see the web interface for the calculator.
3. Read the app's source code at [localhost:8080/docs](http://localhost:8080/docs).
4. Run the app's tests with `lein test`. Read the tests at `test/calculator_api/`.

If not, see instructions [below](#develop).

## Configuration
To configure logging see config/logback.xml. By default, the app logs to stdout and logs/.
To learn more about configuring Logback, read its [documentation](http://logback.qos.ch/documentation.html).


## Run
If you received `calculator-api-0.0.1-SNAPSHOT-standalone.jar`, just run `java -jar filename.jar`.
It should run without any dependency issues. Everything is "statically compiled" inside the `jar` file. You won't need to install `antlr`.
Otherwise, you need to [setup the development environment](#develop).


## Develop

### Install Antlr4
We need to install [antlr-4.9.2](https://www.antlr.org) to compile Grammer and generate parser.
```
$ cd /opt
$ sudo mkdir antlr4
$ sudo chown <username>:<username> antlr4
$ cd antlr4
# Get latest version from https://www.antlr.org/download.html
$ sudo curl -O https://www.antlr.org/download/antlr-X.X.X-complete.jar

# Add these to .bashrc or similar location.
$ export CLASSPATH=".:/usr/local/lib/antlr4/antlr-X.X.X-complete.jar:$CLASSPATH"
$ alias antlr4='java -jar /usr/local/lib/antlr-X.X.X-complete.jar'
$ alias grun='java org.antlr.v4.gui.TestRig'
```


### Compile Antlr4 Grammar
```
cd src/java/grammer/expr
rm *.java *.tokens *.interp *.class # remove what's left over from testing the grammar
antlr4 -package grammar.expr -no-listener -visitor Expr.g4
```


### Test Antlr4 Grammar
```
cd src/java/grammer/expr
rm *.java *.tokens *.interp # remove what's left over from compiling the grammar
antlr4 Expr.g4 && javac *.java
grun Expr prog -gui <file-with-expressions> # GUI
grun Expr prog -tree <file-with-expressions> # Text-based
```
If you don't supply any file, you can type your expression directly, then press `ctrl+d` to get the result.


### Sass
Install Sass preprocessor:
```
sudo npm install -g sass
```

Watch (for development):
```
lein sass-watch
# or
sass --style=compressed --watch src/sass/style.sass:resources/public/vendor/css/style.min.css
```
Release compile:
```
lein sass
# or
sass --style=compressed src/sass/style.sass:resources/public/vendor/css/style.min.css
```


### Generate docs
```
$ lein marg
```


### front-end
Development (front-end source files are located in `src/front-end`)
```
shadow-cljs watch app
```
Release compile:
```
npm run release
```
For more information about front-end, check [FRONTEND.md](/FRONTEND.md)


### Run the program in development environment
1. Start a new REPL: `lein repl`
2. Start your service in dev-mode: `(def dev-serv (run-dev))`
3. Connect your editor to the running REPL session.
   Re-evaluated code will be seen immediately in the service.


### Compile everything for release/deploy
```
lein do clean, marg, sass, front-end, uberjar
```
Then you can run/release/deploy the `standalone` jar file in `target/` directory.


## [Docker](https://www.docker.com/) container support
1. Configure your service to accept incoming connections (edit service.clj and add  ::http/host "0.0.0.0" )
2. Build an uberjar of your service. (see above)
3. Build a Docker image: `sudo docker build -t calculator-api .`
4. Run your Docker image: `docker run -net host calculator-api`


## [OSv](http://osv.io/) unikernel support with [Capstan](http://osv.io/capstan/)
1. Build and run your image: `capstan run -f "8080:8080"`


Once the image it built, it's cached.  To delete the image and build a new one:
1. `capstan rmi calculator-api; capstan build`
