    USAGE:

        ./android_string.sh xls path [-a] [-t] [-o] [path] [-d] [-h]

        this command will create a xxx.xls at xls direcotry according the path of user input!

            path         the app dir (absolute path) which contains strings.xml or strings.xml
            -a           the path is android root path : only find strings.xml on packages/apps, frameworks/base/core/res/res, frameworks/packages
            -t           all strings of strings.xml(include translatable=false)
            -o path      define output dir
            -d           print some debug infos
            -h           print help




        ./android_string.sh xml  [path] [-r] [-a] [-o path] [-d] [-h] [-x path]

        this command will create some string.xml at output dirctory(default is xml which you can changed by param -o path) from the xxx.xls which in current directory or the params path!

        path         the app dir (absolute path) which contains strings.xml or strings.xml can absent
        -a           the path is android root path : only find strings.xml on packages/apps, frameworks/base/core/res/res, frameworks/packages
        -x path      the dir which has a file *.xls(the current dir if absend)
        -r           all strings of strings.xml(include translatable=false)
        -o path      define output dir
        -d           print some debug infos
        -h           print help


    some times you can find an error.txt int strings.xml dir,it records some error when write strings.xml
