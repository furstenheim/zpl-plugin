const fs = require('fs')
const data = fs.readFileSync('../resources/ZPLII-Prog.txt').toString().split('\n')

const mainDocument = data.slice(1283)
let isOddPage = false
let startPageIndex = 0
let currentCode
const commands = []
for (let i = 0; i < mainDocument.length; i++){
  let line = mainDocument[i];
  if (line.startsWith('\f  ')) {
    isOddPage = true
    startPageIndex = i
    console.log('odd page', toIndex(i))
  }
  if (line.match(/^\f\d+/)) {
    isOddPage = false
    startPageIndex = i
    console.log('even page', toIndex(i))
  }
  let isCommand
  if (isOddPage) {
    isCommand = line.match(/^([~^]\w{1,3})\s*$/)
  } else {
    isCommand = line.match(/^ {5}([~^]\w{1,3})\s*$/)
  }
  if (isCommand && i === startPageIndex + 1) {
    console.log('Is command ignoring')
    continue
  }

  if (isCommand) {
    /*const previousLine = mainDocument[i - 1] || ''
    const nextLine = mainDocument[i + 1] || ''
    if (previousLine.trim().startsWith('^') || nextLine.trim().startsWith('^')) {
      console.log('Most probably an example, ignoring')
      continue
    }*/

    currentCode = isCommand[1]
    console.log(currentCode, toIndex(i))
    commands.push(currentCode)
  }

}
console.log(commands)
function toIndex (i) {
  return i + 1283
}

function matchCommand (line) {
  return line.match(/^\s*([~^]\w{1,3})\s*$/)
}
