const fs = require('fs')
const data = fs.readFileSync('../resources/ZPLII-Prog.txt').toString().split('\n')

const UNDEFINED = 0
const CODE = 1
const DEFINITION = 2
const DESCRIPTION = 3
const PARAMETERS = 4

const mainDocument = data.slice(1283)
let isOddPage = false
let startPageIndex = 0
let currentCode
let status = UNDEFINED
const commands = []
for (let i = 0; i < mainDocument.length; i++){
  let line = mainDocument[i];
  if (line.startsWith('\f  ')) {
    isOddPage = true
    startPageIndex = i
    console.log('odd page', toIndex(i))
    continue
  }
  if (line.match(/^\f\d+/)) {
    isOddPage = false
    startPageIndex = i
    console.log('even page', toIndex(i))
    continue
  }
  if (status === CODE) {
    currentCode.definition = line
    status = DEFINITION
    continue
  }
  if (status === DEFINITION) {
    currentCode.description.push(line)
    status = DESCRIPTION
    continue
  }

  if (status === DESCRIPTION) {
    if (line.trim() === '') {
      status = UNDEFINED
    } else {
      currentCode.description.push(line)
    }
    continue
  }
  if (line.match(/^\s*Parameters\s*Details\s*/)) {
    status = PARAMETERS
    continue
  }
  if (status === PARAMETERS) {
    if (line.trim() === '') {
      status = UNDEFINED
    } else {
      currentCode.parameters.push(line)
    }
    continue
  }

  if (currentCode && line.trim().startsWith(`Format ${currentCode.code}`)) {
    currentCode.format.push(line)
  }
  let isCommand
  if (isOddPage) {
    isCommand = line.match(/^([~^]\w{1,3})\s*$/)
  } else {
    isCommand = line.match(/^ {5,6}([~^]\w{1,3})\s*$/)
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

    currentCode = {
      code: isCommand[1],
      definition: null,
      format: [],
      description: [],
      parameters: []
    }
    // console.log(currentCode, toIndex(i))
    commands.push(currentCode)
    status = CODE
  }

}
console.log(commands)
function toIndex (i) {
  return i + 1283
}

function matchCommand (line) {
  return line.match(/^\s*([~^]\w{1,3})\s*$/)
}
