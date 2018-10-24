<h1 align="center"><b>jPakBus 0.5</b></h1>

## What is *jPakBus*?
*jPakBus* is a Java implementation of the **PakBus** family of proprietary communications protocols by [Campbell Scientific, Incorporated](https://www.campbellsci.com/).  **PakBus** is used primarily in their line of dataloggers (and instruments incuding embedded dataloggers).  The **PakBus** protocols are connectionless, datagram-oriented protocols that most closely resemble UDP in the TCP/IP networking stack.  

## Why does the world need *jPakBus*?
Well, probably the world doesn't actually need *jPakBus* - but if you happen to be attempting the same thing the author was, you might find it useful.  I purchased a [WeatherHawk 621](https://www.weatherhawk.com/product/weatherhawk-621-wireless-heated-weather-station/) weather station from Campbell Scientific, and I wanted to integrate its lovely stream of data with other systems at my residence.  Out-of-the-box software is oriented towards displaying the weather data and pushing it up to sites like [Weather Underground](https://www.wunderground.com/) &ndash; not the sort of database-building and integration that I needed.  *jPakBus* was my answer to that need.

## What, exactly, does *jPakBus* do?
Communicating with a [Campbell Scientific, Incorporated](https://www.campbellsci.com/) datalogger is a non-trivial process.  It's not exactly *hard*, mainly just tedious and different than most of today's systems.  **PakBus** is a relatively old set of protocols, greatly pre-dating the standards in wide use today, such as JSON.  It's also a set of protocols designed for use over slow serial links, so it pays more attention to data efficiency than most modern protocols.  Also, the datalogger endpoints are sometimes very low power devices, with limited CPU and memory &ndash; placing even more constraints on those protocols.

*jPakBus* hides most of these details from a programmer who is trying to just use a **PakBus** device.  In this project you'll find my own WeatherHawk implementation as an example.  To write something like that, if you have *jPakBus* then you don't need to know any of the details of **PakBus** serialization, deserialization, packet construction, and so on.  All you have to know is what data (and data types) are in what tables, and a few pesky details like the address of the datalogger.  The rest is all handled by *jPakBus*.

## A big, big thank you...
When I first started this project, I had little in the way of documentation.  Vast swathes of **PakBus** lore were invisible to me.  I started asking some questions with the one person at Campbell Scientific I knew: Jeff Balls, the product manager for the WeatherHawk product line.  Over a period of a few weeks, he answered a stream of (no doubt annoying) questions from this ignorant and ancient engineer.  He showered documentation on me, referred my more challenging questions to engineers and techs within the company.  In the process, every single one of my (many) questions got answered, and answered authoritatively.  I *could* have done this empirically and by reverse-engineering, but I didn't need to thanks to the great customer service from Campbell Scientific.  Thanks, folks; 'tis much appreciated!

## A big, big caveat...
I have tested this code *only* with the WeatherHawk 621, which has an embedded model CR200 datalogger.  While I think it's likely that this code will work with other dataloggers, I have no access to them and have not tested it.  Also, other datalogger models have some capabilities that I did not implement, as I have no way to test them.  Extending *jPakBus* to handle those should be a straightforward exercise, as all the building blocks (and plenty of examples) are here to do so.

## A note for anyone digging into the code...
The heart of this code is in the packages *types* and *values*.  These packages contain the classes that let you build models of the **PakBus** message data.  Those models handle the serialization and deserialization.  If you understand these two packages, the rest of *jPakBus* will be a cakewalk.

## Dependencies
The only dependencies *jPakBus* has (other than the standard Java libraries) is on the [jSerialComm](http://fazecast.github.io/jSerialComm/) project, which provides platform-independent serial port access.  I've tested this on OSX and Linux (Ubuntu).

## Why is *jPakBus*'s code so awful?
Mainly because the author has serious deficiencies in aptitude, intelligence, and knowledge (not to mention choices of hobbies).  If you have questions, you may contact me at tom_at_dilatush_dot_com.  Polite questions without insults to my ancestors are much more likely to be answered.  If you have extensions or fixes that you'd like me to incorporate here, by all means get in touch.

## How is *jPakBus* licensed?
*jPakBus* is licensed with the quite permissive MIT license:
> Created: October 1, 2018
> Author: Tom Dilatush <tom@dilatush.com>  
> License: MIT
> 
> Copyright 2018 Tom Dilatush
> 
> Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
> documentation files (the "Software"), to deal in the Software without restriction, including without limitation
> the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
> to permit persons to whom the Software is furnished to do so.
> 
> The above copyright notice and this permission notice shall be included in all copies or substantial portions of
> the Software.
> 
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
> THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE A
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
> TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
> SOFTWARE.
