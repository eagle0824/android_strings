    USAGE:
          ./build.sh [command] [path] {params}
    >>>>>>>    command  cxls    <<<<<<<<<<<<<<<
           command cxls will create a xxx.xls at xls direcotry according the path of user input!
    
           path is the app dir (absolute path) which contains strings.xml or strings.xml
           params is Optional -a or -t 
           params -a is stand for the path is android root path
           params -t is stand for include all strings of strings.xml(include translatable=false)

	example: 
		./build.sh cxls app_path
    
    
    >>>>>>>    command  cxml    <<<<<<<<<<<<<<<
           command cxml will create some string.xml at data dirctory from the xxx.xls which in current directory!
           you must put your xls file in the current dir
           if has start and end,start must gratter end,start and end can't has only one,
           if not has start and end default is all xls files
    
	example: 
		./build.sh cxml
    
    >>>>>>>    command  rcxml    <<<<<<<<<<<<<<<
           command rcxml will create some string.xml which the id come from the path  value/strings.xml and value from the xxx.xls 
           path is the dir which contains strings.xml or strings.xml absolute path
           params is Optional -a or -t 
           params -a is stand for the path is android root path
           params -t is stand for include all strings of strings.xml(include translatable=false)
           the output dir data(id and vlaue are both from xls file) and output dir rwdata(which id from values/string and value from xls file)
    
	example: 
		./build.sh rcxml app_path

    some times you can find an error.txt int strings.xml dir,it records some error when write strings.xml
